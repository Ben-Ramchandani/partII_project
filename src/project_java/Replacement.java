package project_java;

public class Replacement {
	public String from;
	public String to;
	
	public Replacement(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public String replace(String in) {
		return in.replaceAll(from, to);
	}
}
