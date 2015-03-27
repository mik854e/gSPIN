import java.util.ArrayList;

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
}