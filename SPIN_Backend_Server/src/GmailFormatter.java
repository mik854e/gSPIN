import java.util.ArrayList;
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

		int depth = 0;
		String parent = "NULL";
		for (Message m : msgs) {
			parent = formatMessage(m, sb, depth, parent);
			depth++;
		}
		sb.append("</thread>");

		return sb.toString();
	}

	public static String formatMessage(Message m, StringBuilder sb, int depth, String parent) {
		sb.append("<message>");
		sb.append(System.lineSeparator());

		sb.append("<depth>"+depth+"</depth>");
		sb.append(System.lineSeparator());

		sb.append("<parent>"+parent+"</parent>");
		sb.append(System.lineSeparator());

		sb.append("<message_id>"+m.getId()+"</message_id>");
		sb.append(System.lineSeparator());

		MessagePart mp = m.getPayload();
		ArrayList<MessagePartHeader> headers = (ArrayList<MessagePartHeader>) mp.getHeaders();

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

		for (MessagePartHeader mph : headers) {
			if (mph.getName().equals("To")) {
				mat = p.matcher(mph.getValue());
				if (mat.find()) {
					String name = mat.group(1);
					String email = mat.group(2);
					sb.append("<to name=\""+name+"\" id=\""+email+"\" address=\""+email+"\" />");
					sb.append(System.lineSeparator());
				}
			}
		}

		String content = StringUtils.newStringUtf8(Base64.decodeBase64(mp.getParts().get(0).getBody().getData()));
System.out.println(content);
		content = StringEscapeUtils.escapeXml(content);

		sb.append("<content>");
		sb.append(System.lineSeparator());
		sb.append(content);
		sb.append(System.lineSeparator());
		sb.append("</content>");
		sb.append(System.lineSeparator());



		sb.append("</message>");
		sb.append(System.lineSeparator());

		return m.getId().toString();
	}	
}