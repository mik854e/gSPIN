import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonBuilderFactory;

public class OutputFormatter {

  public static String formatOutput(File f, HashMap<String, String> thread_info) {
    StringBuilder sb_html = new StringBuilder();
//    sb_html.append("<div id=\"power-graph\" class=\"power-graph\"><center><canvas id=\"springydemo\" width=\"650px\"/></center></div>");
    sb_html.append("<div class=\"container-fluid\"><div class=\"row\"><div class=\"col-xs-4\">");
    sb_html.append("<div class=\"checkbox\"><center><label><input id=\"request-cb\" type=\"checkbox\" value=\".request\" checked>Request</label></center>");
    sb_html.append("</div></div><div class=\"col-xs-4\"><div class=\"checkbox\">");
    sb_html.append("<center><label><input id=\"conventional-cb\" type=\"checkbox\" value=\".conventional\" checked>Conventional</label></center></div>");
    sb_html.append("</div><div class=\"col-xs-4\"><div class=\"checkbox\"><center><label><input id=\"inform-cb\" type=\"checkbox\" value=\".inform\" checked>Inform</label></center>");
    sb_html.append("</div></div></div></div>");
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
          sb_html.append(formatMessage(m));
        }
      }

      StringBuilder sb_power = new StringBuilder();
      sb_power.append("[");
      nl = doc.getElementsByTagName("powerannotations");
      if (nl != null && nl.getLength() > 0) {
        Node node = (Node) nl.item(0);
        nl = node.getChildNodes(); //getElementsbyTagName("powerannotation");
        if (nl != null && nl.getLength() > 0) {
          for (int i = 0 ; i < nl.getLength(); i++) {
            node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
              Element pa = (Element)nl.item(i);
              PowerAnnotation power_annotation = getPowerAnnotation(pa, thread_info);
              sb_power.append(formatPowerAnnotation(power_annotation));
            }
          }
        }
      }
      sb_power.deleteCharAt(sb_power.length()-1);
      sb_power.append("]");
      JsonBuilderFactory factory = Json.createBuilderFactory(null);
      JsonObject json = factory.createObjectBuilder().add("html", sb_html.toString()).add("graph", sb_power.toString()).build();

      return json.toString();
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
  }

  private static String formatPowerAnnotation(PowerAnnotation pa) {
    StringBuilder sb = new StringBuilder();
    sb.append("[\""+pa.superior+"\",\""+pa.subordinate+"\"],");
    return sb.toString();
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

  private static PowerAnnotation getPowerAnnotation(Element pa, HashMap<String, String> thread_info) {
    String type = pa .getAttribute("type");
    String person1 = pa.getAttribute("person1");
    String person2 = pa.getAttribute("person2");

    String superior = thread_info.get(person1);
    String subordinate = thread_info.get(person2);

    return new PowerAnnotation(type, superior, subordinate);
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