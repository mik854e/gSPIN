import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonBuilderFactory;

class Message {

	public int depth;
	public String parent;
	public String message_id;
	public String date_time;
	public String subject;
	public Person from;
	public ArrayList<Person> to;
	public ArrayList<Person> cc;
	public ArrayList<DFU> content;

	public String temp;

	public Message(int depth, String parent, String message_id, String date_time, String subject,
					Person from, ArrayList<Person> to, ArrayList<Person> cc, ArrayList<DFU> content) {
		this.depth = depth;
		this.parent = parent;
		this.message_id = message_id;
		this.date_time = date_time;
		this.subject = subject;
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.content = content;
	}

	public String toString() {
		StringBuilder sb_to = new StringBuilder();
		StringBuilder sb_body = new StringBuilder();

		for (Person p : this.to) {
      		sb_to.append(p.name+", ");
    	}
    	for (Person p : this.cc) {
      		sb_to.append(p.name+", ");
    	}

    	for (DFU dfu : this.content) {
    		sb_body.append(dfu.toString());
    	}

		JsonBuilderFactory factory = Json.createBuilderFactory(null);
      	JsonObject json = factory.createObjectBuilder().add("subject", subject).add("from", from.name).add("to", sb_to.toString()).add("body", sb_body.toString()).build();
		
		return json.toString();
	}
}
