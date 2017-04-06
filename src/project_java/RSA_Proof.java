package project_java;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class RSA_Proof {
	public final BigInteger T;
	public final BigInteger M;
	
	public RSA_Proof(BigInteger T, BigInteger M) {
		this.T = T;
		this.M = M;
	}
	
	public RSA_Proof(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		this.T = new BigInteger(br.readLine(), 16);
		this.M = new BigInteger(br.readLine(), 16);
	}
	
	public RSA_Proof(String fileName) throws IOException {
		this(new FileInputStream(fileName));
	}
	
	@Override
	public String toString() {
		return "\"" + toETHHexString(T) + "\", \"" + toETHHexString(M) + "\"";
	}
	
	private String toETHHexString(BigInteger i) {
		String str = i.toString(16);
		while(str.length() < 64) {
			str = "0" + str;
		}
		return "0x" + str;
	}
	
	public void printTo(PrintStream out) {
		out.println(this.toString());
	}
}
