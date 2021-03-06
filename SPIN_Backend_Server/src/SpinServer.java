import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.sun.net.httpserver.HttpHandler;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.io.FileUtils;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.ListThreadsResponse;

public class SpinServer {
  public static void main(String[] args) throws Exception {
    int PORT = 8230;
    InetSocketAddress addr = new InetSocketAddress(PORT);
    HttpServer server = HttpServer.create(addr, 0);
    server.createContext("/", new MyHandler());
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
    System.out.println("Server is listening on port " + PORT);
  }
}

class MyHandler implements HttpHandler {
  String USER = "me";

  public Map<String, String> getParameters(String body) {
    Map<String, String> params = new HashMap<String, String>();
    String[] raw_params = body.split("&");

    for (String param : raw_params) {
      String[] kv_pair = param.split("=");
      String key = kv_pair[0];
      String value = kv_pair[1];

      params.put(key, value);
    }

    return params;
  }

  public Gmail getGmailService(String token) {
    // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
    final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    final String APP_NAME = "SPIN";
    // Path to the client_secret.json file downloaded from the Developer Console
    final String CLIENT_SECRET_PATH = "client_secret.json";


    GoogleClientSecrets clientSecrets;

    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    try {
      clientSecrets = GoogleClientSecrets.load(jsonFactory,  new FileReader(CLIENT_SECRET_PATH));
    }
    catch (IOException e) {
      System.out.println("Couldn't read " + CLIENT_SECRET_PATH);
    }

    GoogleCredential credential = new GoogleCredential().setAccessToken(token);

    // Create a new authorized Gmail API client
    Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                                .setApplicationName(APP_NAME).build();
    return service;
  }

  public String writeInputToFile(String parsed_thread) {
    File dir_in = null;
    String dir_name = null;
    boolean is_success = false;
    while (!is_success) {
      dir_name = RandomStringUtils.randomAlphanumeric(8);
      dir_in = new File("EndToEndSystem/SPIN_TrialIn/"+dir_name);
      is_success = dir_in.mkdir();
    }
    File input_file = new File(dir_in, "input.txt");
    try {
      BufferedWriter input_writer = new BufferedWriter(new FileWriter(input_file));
      input_writer.write(parsed_thread);
      input_writer.close();
    }
    catch (Exception e) {
      System.out.println("Error writing input file.");
    }
    finally {
      return dir_name;
    }
  }

  public String readRequest(HttpExchange exchange) {
    String body = "";
    try {
      InputStream requestBody = exchange.getRequestBody();
      DataInputStream in = new DataInputStream(requestBody);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      StringBuilder sbr = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        //System.out.println(line);
        sbr.append(line);
        //sbr.append(System.lineSeparator());
      }
      body = sbr.toString();

      reader.close();
      requestBody.close();
    }
    catch (Exception e) {
      System.out.println("Error reading request.");
    }
    finally {
      return body;
    }
  }

  public void handle(HttpExchange exchange) throws IOException {
    System.out.println("Request");
    String requestMethod = exchange.getRequestMethod();
    if (requestMethod.equalsIgnoreCase("POST")) {
      Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set("Content-Type", "application/json");//"text/plain");
      responseHeaders.set("Access-Control-Allow-Origin", "*");

      exchange.sendResponseHeaders(200, 0);

      String body = readRequest(exchange);

      Map<String, String> params = getParameters(body);
      String threadID = params.get("threadID");
      String token = params.get("token"); //URLDecoder.decode(params.get("token"), "UTF-8");;

      //System.out.println(token);
      //System.out.println(threadID);

      String dir_name = "";
      HashMap<String, String> thread_info = null;
      try {
        Gmail service = getGmailService(token);
        Thread thread = service.users().threads().get(USER, threadID).execute();
        //System.out.println(thread.toPrettyString());
        thread_info = GmailFormatter.getThreadInfo(thread);
        String parsed_thread = GmailFormatter.formatThread(thread, thread_info);
        //System.out.println(parsed_thread);
        dir_name = writeInputToFile(parsed_thread);
      }
      catch (Exception e) {
        System.out.println("Error sending Gmail request or writing to file.");
        e.printStackTrace();
        return;
      }

      try {
        File dir = new File("EndToEndSystem");
        String dirs = "SPIN_TrialIn/"+dir_name+" SPIN_TrialOut/"+dir_name;
        
        // Process input
        Runtime rt = Runtime.getRuntime();
        String proc = "java -jar SPIN_PairORGHP_Runner_032215.jar Runner.properties " + dirs;
        Process pr = rt.exec(proc, null, dir);
        int exit_val = pr.waitFor();

        // Process output XML and convert into response for frontend.
        File dir_in = new File("EndToEndSystem/SPIN_TrialIn/"+dir_name);
        File dir_out = new File("EndToEndSystem/SPIN_TrialOut/"+dir_name);

        // Create response from output of SPIN system.
        File output_file = new File(dir_out, "input.txt.tagged");
        String formatted_output = OutputFormatter.formatOutput(output_file, thread_info);
//System.out.println(formatted_output);

        // Delete SPIN directories.
        FileUtils.deleteDirectory(dir_in);
        FileUtils.deleteDirectory(dir_out);
        
        // Send response.
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(formatted_output.getBytes());
        responseBody.close();
      }

      catch (Exception e) {
        e.printStackTrace();
        return;
      }

    }
  }

}