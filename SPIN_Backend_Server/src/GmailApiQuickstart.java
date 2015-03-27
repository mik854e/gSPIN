import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.ListThreadsResponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class GmailApiQuickstart {

  // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
  private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
  private static final String APP_NAME = "SPIN";
  // Email address of the user, or "me" can be used to represent the currently authorized user.
  private static final String USER = "me";
  // Path to the client_secret.json file downloaded from the Developer Console
  private static final String CLIENT_SECRET_PATH = "client_secret.json";

  private static GoogleClientSecrets clientSecrets;

  public static void main(String [] args) throws IOException {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    clientSecrets = GoogleClientSecrets.load(jsonFactory,  new FileReader(CLIENT_SECRET_PATH));

    // Allow user to authorize via url.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, jsonFactory, clientSecrets, Arrays.asList(SCOPE))
        .setAccessType("online")
        .setApprovalPrompt("auto").build();

    String url = flow.newAuthorizationUrl().setRedirectUri("https://www.example.com/oauth2callback")
        .build();
    System.out.println("Please open the following URL in your browser then type"
                       + " the authorization code:\n" + url);

    // Read code entered by user.
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String code = br.readLine();

  

    // Generate Credential using retrieved code.
//    GoogleTokenResponse response = flow.newTokenRequest(code)
//        .setRedirectUri("https://www.example.com/oauth2callback").execute();
//    GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);
    GoogleCredential credential = new GoogleCredential().setAccessToken(code);


    // Create a new authorized Gmail API client
    Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(APP_NAME).build();

    // Retrieve a page of Threads; max of 100 by default.
    //ListThreadsResponse threadsResponse = service.users().threads().list(USER).execute();
    //List<Thread> threads = threadsResponse.getThreads();

    Thread thread = service.users().threads().get(USER, "14bd127d951e9be0").execute();
    String output = GmailFormatter.formatThread(thread);
    System.out.println(output);
    System.out.println("YESSSSSSSSS");
    //System.out.println(thread.toPrettyString());



    // Print ID of each Thread.
    //for (Thread thread : threads) {
    //  System.out.println("Thread ID: " + thread.getId());
    //}
  }

}