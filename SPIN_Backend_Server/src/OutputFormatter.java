import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


public class OutputFormatter {

  public static String formatOutput(File f) {
    StringBuilder sb = new StringBuilder();
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document dom = db.parse(f);
      Element doc = dom.getDocumentElement();

      NodeList nl = doc.getElementsByTagName("message");
      if (nl != null && nl.getLength() > 0) {
        for (int i = 0 ; i < nl.getLength(); i++) {
          Element msg = (Element)nl.item(i);
          Message m = getMessage(msg);
          sb.append(formatMessage(m));
        }
      }
      return sb.toString();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
     
    return "ERROR";
  }

  private static String formatMessage(Message m) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<div class=\"panel panel-default\">");

    // Subject
    sb.append("<div class=\"panel-heading\"><h3 class=\"panel-title\">"+m.subject+"</h3></div>");
    sb.append("<div class=\"panel-body\">");

    // From
    sb.append("<b>"+m.from.name+"</b><br>");

    // To
    sb.append("to ");
    for (Person p : m.to) {
      sb.append(p.name+", ");
    }
    for (Person p : m.cc) {
      sb.append(p.name+", ");
    }
    sb.append("<br><br>");

    // Body
    sb.append("<table><col style=\"width:150px\">");
    for (DFU dfu : m.content) {
      if (dfu.da.equals("FILLER")) {
        sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
      }
      else {
      //sb.append("<font color=\""+color+"\">"+dfu.DA+": </font>");
        String da_class = getDAClass(dfu.da);
        sb.append("<tr>");
        sb.append("<td><div class=\""+da_class+"\">"+dfu.da+"</div></td>");

        sb.append("<td>");

        if (dfu.odp)
          sb.append("<mark>"+dfu.text+"</mark> (ODP)");
        else
          sb.append(dfu.text);

        sb.append("</td>");
        sb.append("</tr>");
      }

      //sb.append("<br>");
    }
    sb.append("</table>");

    sb.append("</div></div>");//sb.append("<br><br></div></div>");

    return sb.toString();
    /*
    sb.append("Subject: " + m.subject);
    sb.append("<br>");
    sb.append("From: " + m.from.name);
    sb.append("<br>");
    String to = "To: ";
    for (Person p : m.to) {
      to += p.name + ", ";
    }
    sb.append(to);
    sb.append("<br>");
    sb.append("<br>");

    for (DFU dfu : m.content) {
      
      String s = "[" + dfu.DA + "] " + dfu.text;
      if (dfu.ODP) {
        s += " (ODP)";
      }
      
      /*
      String color = null;
      String s = null;
      if (dfu.DA.equals("Request-Information"))
        color = "blue";
      else if (dfu.DA.contains("Request"))
        color = "red";
      if (color != null)
        s = "<font color=\"" + color + "\">" + dfu.text + "</font>";
      else
        s = dfu.text;

      if (dfu.ODP) {
        sb.append("<b>");
        sb.append(s);
        sb.append("</b>");
      }
      else
        sb.append(s);
      
      
    sb.append(s);
    sb.append("<br>");
    }

    sb.append("<br>");
    sb.append("<br>");

    */
  }

  private static String getDAClass(String da) {
    String da_class;
    if (da.contains("Request"))
      da_class = "request";
    else if (da.equals("Conventional"))
      da_class = "conventional";
    else if (da.equals("Inform")) 
      da_class = "inform";
    else 
      da_class = "other";

    return da_class;
  }

  private static Message getMessage(Element msg) {
    int depth = 0; //getIntValue(msg, "depth");
    String parent = "0"; //getIntValue(msg, "parent");
    String message_id = "0"; //getIntValue(msg, "message_id");

    String date_time = getTextValue(msg, "date_time");
    String subject = getTextValue(msg, "subject");
    Person from = getPerson((Element) msg.getElementsByTagName("from").item(0));
    ArrayList<Person> to = getPeople(msg.getElementsByTagName("to"));
    ArrayList<Person> cc = getPeople(msg.getElementsByTagName("cc"));
    ArrayList<DFU> content = getContent((Element) msg.getElementsByTagName("content").item(0));

    return new Message(depth, parent, message_id, date_time, subject, from, to, cc, content);
  }

  private static ArrayList<DFU> getContent(Node content) {
    ArrayList<DFU> c = new ArrayList<DFU>();

    NodeList nl = content.getChildNodes();//getElementsByTagName("DFU");
    if (nl != null && nl.getLength() > 0) {
        for (int i = 0 ; i < nl.getLength(); i++) {
          Node node = (Node) nl.item(i);
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            DFU d;
            Element dfu = (Element) node;
            if (dfu.getTagName().equals("FILLER")) 
              d = getFiller(dfu);
            else
              d = getDFU(dfu);
            c.add(d); 
          }
        }
    }
    return c;
  }

  private static ArrayList<Person> getPeople(NodeList nl) {
    ArrayList<Person> to = new ArrayList<Person>();
    if (nl != null && nl.getLength() > 0) {
      for (int i = 0 ; i < nl.getLength(); i++) {
        Element person = (Element)nl.item(i);
        Person p = getPerson(person);
        to.add(p); 
      }
    }
    return to;
  }

  private static Person getPerson(Element person) {
    String name = person.getAttribute("name");
    String id = person.getAttribute("id");
    String address = person.getAttribute("address");

    return new Person(name, id, address);
  }

  private static DFU getDFU(Element dfu) {
    String DA = dfu.getAttribute("DA");
    NodeList odp_list = dfu.getElementsByTagName("ODP");
    boolean ODP;
    String text;
    if (odp_list != null && odp_list.getLength() > 0) {
      ODP = true;
      text = getTextValue(dfu, "ODP");
    }
    else {
      ODP = false;
      text = dfu.getTextContent();
    }

    return new DFU(DA, ODP, text);
  }

  private static DFU getFiller(Element dfu) {
    String text = dfu.getTextContent();
    return new DFU("FILLER", false, text);
  }

  private static String getTextValue(Element ele, String tagName) {
	String textVal = null;
	NodeList nl = ele.getElementsByTagName(tagName);
	if (nl != null && nl.getLength() > 0) {
	  Element el = (Element)nl.item(0);
	  textVal = el.getFirstChild().getNodeValue();
	}

	return textVal;
  }

  private static int getIntValue(Element ele, String tagName) {
    return Integer.parseInt(getTextValue(ele,tagName));
  }
}