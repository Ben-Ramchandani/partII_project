package project_java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RSA_POR {
	
	public static int maxChunksBits = 32;
	
	public BigInteger N;
	public BigInteger g;
	public BigInteger e, d;
	public byte[] v;
	
	public BlockStream in;
	
	
	public RSA_POR(File keyFile, BlockStream in) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(keyFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       lines.add(line);
		    }
		} catch(IOException e) {
			System.exit(2);
		}
		N = new BigInteger(lines.remove(0), 16);
		g = new BigInteger(lines.remove(0), 16);
		e = new BigInteger(lines.remove(0), 16);
		d = new BigInteger(lines.remove(0), 16);
		int vLen = 256;
		v = new byte[vLen];
		byte[] vInt = new BigInteger(lines.remove(0), 16).toByteArray();
		System.arraycopy(vInt, 0, v, vLen - vInt.length, vInt.length);

		this.in = in;
	}
	
	public BigInteger gethW_i(int i) {
		//return new BigInteger(Util.HMAC(v, Util.toBytes(i)));
		return BigInteger.valueOf(2);
	}
	
	public BigInteger tagChunk(int chunkIndex) {
		byte[] chunkBytes = in.getChunk(chunkIndex);
		BigInteger chunk = new BigInteger(chunkBytes);
		BigInteger hW_i = gethW_i(chunkIndex);
		BigInteger g_to_chunk = g.modPow(chunk, N);
		BigInteger mult = hW_i.multiply(g_to_chunk);
		return mult.modPow(d, N);
	}

	
	public static void printBigInteger(BigInteger num, PrintStream out) {
		out.println(num.toString(16));
	}
}
