import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;
import java.util.Date;

import java.text.SimpleDateFormat;

import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class GmailFormatter {

	public static String formatThread(Thread thread, HashMap<String, String> thread_info) {
		StringBuilder sb = new StringBuilder();
		sb.append("<thread>");
		sb.append(System.lineSeparator());

		sb.append("<thread_id>"+thread.getId()+"</thread_id>");
		sb.append(System.lineSeparator());

		ArrayList<Message> msgs = (ArrayList<Message>) thread.getMessages();

		for (Message m : msgs) {
			formatMessage(m, sb, thread_info);
		}
		sb.append("</thread>");

		return sb.toString();
	}

	public static HashMap<String, String> getThreadInfo(Thread thread) {
		ArrayList<Message> msgs = (ArrayList<Message>) thread.getMessages();
		HashMap<String, String> thread_info = new HashMap<String, String>();

		for (Message m : msgs) {
			MessagePart mp = m.getPayload();
			ArrayList<MessagePartHeader> headers = (ArrayList<MessagePartHeader>) mp.getHeaders();

			// Get a map from email addresses to names.
			Pattern p = Pattern.compile("\"?([A-Za-z0-9.@ ]+)\"? <(.+@.+)>");
			Matcher mat;
			for (MessagePartHeader mph : headers) {
				if (mph.getName().equals("To") || mph.getName().equals("From") || mph.getName().equals("CC")) {
					String[] recipients = mph.getValue().split(", ");
//System.out.println(Arrays.toString(recipients));
					for (String recipient : recipients) {
						mat = p.matcher(recipient);
						if (mat.find()) {
							String name = mat.group(1);
							String email = mat.group(2);
							thread_info.put(email, name);
						}
						else {
							System.out.println("NO REGEX MATCH: " + recipient);
						}
					}
				}
			}

			String msg_id = null;
			String parent_id = null;
			// Get a map from threadIDs to thread depths.
			for (MessagePartHeader mph : headers) {
				if (mph.getName().toLowerCase().equals("message-id")) {
					msg_id = trimID(mph.getValue());
//System.out.println("GET MSGID: "+msg_id);
					break;
				}
			}

			for (MessagePartHeader mph : headers) {
				if (mph.getName().equals("In-Reply-To")) {
					parent_id = trimID(mph.getValue());
//System.out.println("GET PID: "+msg_id);
					break;
				}
			}

			if (parent_id == null) {
//System.out.println("NO PID");
				thread_info.put(msg_id, "0");
			}
			else {
//System.out.println("PID: "+parent_id);
				String depth_val = thread_info.get(parent_id);
				if (depth_val == null)
					depth_val = "0";
				int depth = Integer.parseInt(depth_val) + 1;
				thread_info.put(msg_id, depth+"");
			}
//System.out.println(thread_info.toString());
		}

//System.out.println("THREAD INFO COMPLETE");
		return thread_info;
	}

	private static String trimID(String id) {
		Pattern p = Pattern.compile("<(.+)>");
		Matcher mat;

		mat = p.matcher(id);
		if (mat.find()) {
			return mat.group(1);
		}
//System.out.println("TRIMID FAILED");
		return "";
	}

	private static String getMessageContent(Message m) {
		MessagePart mp = m.getPayload();
		String content = "";
        if (mp.getMimeType().contains("text/")) {
            content = mp.getBody().getData();
        } 
        else if (mp.getMimeType().contains("multipart/alternative")) {
            // prefer html text over plain text
			List<MessagePart> mp_parts = mp.getParts();           
            for (int i = 0; i < mp_parts.size(); i++) {
                MessagePart mp_part = mp_parts.get(i);
                if (mp_part.getMimeType().contains("text/html")) {
                	String text = mp_part.getBody().getData();
                	if (text != null)
                		content = text;
                } else if (mp_part.getMimeType().contains("text/plain")) {
                    String text = mp_part.getBody().getData();
                    if (text != null) {
                        content = text;
                        break;
                    }
                }
            }
        } else if (mp.getMimeType().contains("multipart/")) {
			List<MessagePart> mp_parts = mp.getParts();           
            for (int i = 0; i < mp_parts.size(); i++) {
            	MessagePart mp_part = mp_parts.get(i);
               	String text = mp_part.getBody().getData();
                if (text != null) {
                    content = text;
                }
            }
        }
       	String temp = StringUtils.newStringUtf8(Base64.decodeBase64(content));
       	return StringEscapeUtils.escapeXml(temp);
    }

	public static void formatMessage(Message m, StringBuilder sb, HashMap<String, String> thread_info) {
		MessagePart mp = m.getPayload();
		ArrayList<MessagePartHeader> headers = (ArrayList<MessagePartHeader>) mp.getHeaders();

		sb.append("<message>");
		sb.append(System.lineSeparator());

		String depth = "0";
		String parent_id = null;
		String msg_id = null;

		for (MessagePartHeader mph : headers) {
			if (mph.getName().toLowerCase().equals("message-id")) {
				msg_id = trimID(mph.getValue());
				break;
			}
		}

		depth = thread_info.get(msg_id);
//System.out.println("DEPTH: " + depth);


		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("In-Reply-To")) {
				parent_id = trimID(mph.getValue());
			}
		}

		sb.append("<depth>"+depth+"</depth>");
		sb.append(System.lineSeparator());

		if (parent_id == null) {
			parent_id = "NULL";
		}

//System.out.println("PARENT_ID: "+parent_id);
		sb.append("<parent>"+parent_id+"</parent>");
		sb.append(System.lineSeparator());

//System.out.println("MSG_ID: "+msg_id);
		sb.append("<message_id>"+msg_id+"</message_id>");
		sb.append(System.lineSeparator());


		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("Date")) {
				Date date = new Date(mph.getValue());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date_time = sdf.format(date);

				sb.append("<date_time>"+date_time+"</date_time>");
				sb.append(System.lineSeparator());
				break;
			}
		}

		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("Subject")) {
				sb.append("<subject>"+mph.getValue()+"</subject>");
				sb.append(System.lineSeparator());
				break;
			}
		}

		Pattern p = Pattern.compile("\"?([A-Za-z0-9.@ ]+)\"? <(.+@.+)>");
		Matcher mat;
        
		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("From")) {
				mat = p.matcher(mph.getValue());
				if (mat.find()) {
					String name = mat.group(1);
					String email = mat.group(2);
					sb.append("<from name=\""+name+"\" id=\""+email+"\" address=\""+email+"\" />");
					sb.append(System.lineSeparator());
				}
				break;
			}
		}
		String name;
		String email;
		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("To")) {
				String[] recipients = mph.getValue().split(", ");
				for (String recipient : recipients) {
					mat = p.matcher(recipient);
					if (mat.find()) {
						name = mat.group(1);
						email = mat.group(2);
					}
					else {
//System.out.println("NAME NOT GIVEN: "+ recipient);
//System.out.println(thread_info.toString());
						email = recipient;
						name = thread_info.get(email);
					}
//System.out.println("NO FAILURE ON TO");
					sb.append("<to name=\""+name+"\" id=\""+email+"\" address=\""+email+"\" />");
					sb.append(System.lineSeparator());
				}
			break;
			}
		}

		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("CC")) {
				String[] recipients = mph.getValue().split(", ");
				for (String recipient : recipients) {
					mat = p.matcher(recipient);
					if (mat.find()) {
						name = mat.group(1);
						email = mat.group(2);
					}
					else {
//System.out.println("NAME NOT GIVEN: "+ recipient);
//System.out.println(thread_info.toString());
						email = recipient;
						name = thread_info.get(email);
					}
					sb.append("<cc name=\""+name+"\" id=\""+email+"\" address=\""+email+"\" />");
					sb.append(System.lineSeparator());
				}
			break;
			}
		}
		sb.append("<content>");
		sb.append(System.lineSeparator());

		String content = getMessageContent(m);        

        // VP_HIST (03/29): a silly regex to match the history line
        //		sb.append(content);
        Pattern historypattern = Pattern.compile("On ((Mon|Tue|Wed|Thu|Fri|Sat|Sun|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday), )*(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)");
        if (content != null) { 
            for (String line : content.split("\n")) {
                Matcher historymatcher = historypattern.matcher(line);
                if (historymatcher.find()) {
                    //System.out.println("Found history line =======: " + line);
                    break;
                }
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        // End of VP_HIST


//System.out.println("SURVIVED HISTORY REGEX");
		sb.append(System.lineSeparator());
		sb.append("</content>");
		sb.append(System.lineSeparator());



		sb.append("</message>");
		sb.append(System.lineSeparator());
	}	
}