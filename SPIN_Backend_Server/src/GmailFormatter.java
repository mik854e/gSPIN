import java.util.ArrayList;
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

	public static String formatThread(Thread thread) {
		StringBuilder sb = new StringBuilder();
		sb.append("<thread>");
		sb.append(System.lineSeparator());

		sb.append("<thread_id>"+thread.getId()+"</thread_id>");
		sb.append(System.lineSeparator());

		ArrayList<Message> msgs = (ArrayList<Message>) thread.getMessages();

		HashMap<String, String> thread_info = getThreadInfo(msgs);

		for (Message m : msgs) {
			formatMessage(m, sb, thread_info);
		}
		sb.append("</thread>");

		return sb.toString();
	}

	private static HashMap<String, String> getThreadInfo(ArrayList<Message> msgs) {
		HashMap<String, String> thread_info = new HashMap<String, String>();

		for (Message m : msgs) {
			MessagePart mp = m.getPayload();
			ArrayList<MessagePartHeader> headers = (ArrayList<MessagePartHeader>) mp.getHeaders();

			// Get a map from email addresses to names.
			Pattern p = Pattern.compile("(.+) <(.+@.+)>");
			Matcher mat;
			for (MessagePartHeader mph : headers) {
				if (mph.getName().equals("To") || mph.getName().equals("From")) {
					String[] recipients = mph.getValue().split(", ");
					for (String recipient : recipients) {
						mat = p.matcher(recipient);
						if (mat.find()) {
							String name = mat.group(1);
							String email = mat.group(2);
							thread_info.put(email, name);
						}
					}
				}
			}

			String msg_id = null;
			String parent_id = null;
			// Get a map from threadIDs to thread depths.
			for (MessagePartHeader mph : headers) {
				if (mph.getName().equals("Message-ID")) {
					msg_id = trimID(mph.getValue());
					break;
				}
			}

			for (MessagePartHeader mph : headers) {
				if (mph.getName().equals("In-Reply-To")) {
					parent_id = trimID(mph.getValue());
					break;
				}
			}

			if (parent_id == null) {
				thread_info.put(msg_id, "0");
			}
			else {
				int depth = Integer.parseInt(thread_info.get(parent_id)) + 1;
				thread_info.put(msg_id, depth+"");
			}
		}

		return thread_info;
	}

	private static String trimID(String id) {
		Pattern p = Pattern.compile("<(.+)>");
		Matcher mat;

		mat = p.matcher(id);
		if (mat.find()) {
			return mat.group(1);
		}

		return "";
	}

	public static void formatMessage(Message m, StringBuilder sb, HashMap<String, String> thread_info) {
		MessagePart mp = m.getPayload();
		ArrayList<MessagePartHeader> headers = (ArrayList<MessagePartHeader>) mp.getHeaders();

		sb.append("<message>");
		sb.append(System.lineSeparator());

		String depth;
		String parent_id = null;
		String msg_id = null;

		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("Message-ID")) {
				msg_id = trimID(mph.getValue());
				break;
			}
		}

		depth = thread_info.get(msg_id);

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

		sb.append("<parent>"+parent_id+"</parent>");
		sb.append(System.lineSeparator());

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

		Pattern p = Pattern.compile("(.+) <(.+@.+)>");
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
						email = recipient;
						name = thread_info.get(email);
					}
					sb.append("<to name=\""+name+"\" id=\""+email+"\" address=\""+email+"\" />");
					sb.append(System.lineSeparator());
				}
			break;
			}
		}

		String content = StringUtils.newStringUtf8(Base64.decodeBase64(mp.getParts().get(0).getBody().getData()));
//System.out.println(content);
        
        content = StringEscapeUtils.escapeXml(content);

		sb.append("<content>");
		sb.append(System.lineSeparator());

        // VP_HIST (03/29): a silly regex to match the history line
        //		sb.append(content);
        Pattern historypattern = Pattern.compile("On (Mon|Tue|Wed|Thu|Fri|Sat|Sun), (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d+, \\d+ at \\d+:\\d+ (AM|PM), .*" );
        if (content != null) { // In case of forwarded messages, sometimes content is "null" and this end up showing "NULL" in the output with a conventional tag placed there. Thsi if-condition fixes it, but maybe we should find a cleaner way to handle that.
            for (String line : content.split("\n")) {
                Matcher historymatcher = historypattern.matcher(line);
                if (historymatcher.find()) {
                    System.out.println("Found history line =======: " + line);
                    break;
                }
                sb.append(line);
                sb.append(System.lineSeparator());
            }
        }
        // End of VP_HIST
        
		sb.append(System.lineSeparator());
		sb.append("</content>");
		sb.append(System.lineSeparator());



		sb.append("</message>");
		sb.append(System.lineSeparator());
	}	
}