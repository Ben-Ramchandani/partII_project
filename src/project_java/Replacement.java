package project_java;

import java.math.BigInteger;

public class Replacement {
	public String from;
	public String to;
	
	public Replacement(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public Replacement(String from, BigInteger to) {
		this.from = from;
		this.to = to.toString();
	}
	
	public Replacement(String from, byte[] to) {
		this.from = from;
		this.to = new BigInteger(1, to).toString();
	}
	
	public String replace(String in) {
		return in.replaceAll(from, to);
	}
}
