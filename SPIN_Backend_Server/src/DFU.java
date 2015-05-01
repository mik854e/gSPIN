class DFU {

	public String da;
	public boolean odp;
	public String text;

	public DFU(String da, boolean odp, String text) {
		this.da = da;
		this.odp = odp;
		this.text = text;
	}

	private String getDAClass() {
    String da_class;
    if (this.da.contains("Request"))
      da_class = "request";
    else if (this.da.equals("Conventional"))
      da_class = "conventional";
    else if (this.da.equals("Inform")) 
      da_class = "inform";
    else 
      da_class = "other";

    return da_class;
  }

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (this.da.equals("FILLER")) {
      sb.append("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
    }
    else {
    //sb.append("<font color=\""+color+"\">"+this.DA+": </font>");
      String da_class = getDAClass();
      sb.append("<tr>");
      sb.append("<td><div class=\""+da_class+"\">"+this.da+"</div></td>");

      sb.append("<td>");

      if (this.odp)
        sb.append("<mark>"+this.text+"</mark> (ODP)");
      else
        sb.append(this.text);

      sb.append("</td>");
      sb.append("</tr>");
    }

    return sb.toString();
	}
}