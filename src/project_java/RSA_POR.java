package project_java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RSA_POR {

	public static int maxChunksBits = 32;

	public BigInteger N;
	public BigInteger g;
	public BigInteger e, d;
	public int len_N;
	public byte[] v;

	public ArrayList<BigInteger> test_tags = new ArrayList<BigInteger>();

	public ChunkStream in;

	public RSA_POR(File keyFile, ChunkStream in) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(keyFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
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
		len_N = Integer.parseInt(lines.remove(0), 16);

		this.in = in;
	}

	public BigInteger gethW_i(int i) {
		return new BigInteger(1, Util.HMAC(v, Util.toBytes(i)));
	}

	public BigInteger tagChunk(byte[] chunkBytes, int chunkIndex) {
		// T_i = (h(W_i) * g^(b_i))^d
		BigInteger chunk = new BigInteger(1, chunkBytes);
		BigInteger hW_i = gethW_i(chunkIndex);
		BigInteger g_to_chunk = g.modPow(chunk, N);
		BigInteger mult = hW_i.multiply(g_to_chunk);
		assert (chunk.compareTo(BigInteger.ZERO) >= 0);
		return mult.modPow(d, N);
	}
	
	public BigInteger tagChunk2(byte[] chunkBytes, int chunkIndex) {
		// T_i = (h(W_i)^-1 * g^(b_i))^d
		BigInteger chunk = new BigInteger(1, chunkBytes);
		BigInteger hW_i = gethW_i(chunkIndex);
		BigInteger hW_i_inverse = hW_i.modInverse(N);
		BigInteger g_to_chunk = g.modPow(chunk, N);
		BigInteger mult = hW_i_inverse.multiply(g_to_chunk);
		assert (chunk.compareTo(BigInteger.ZERO) >= 0);
		return mult.modPow(d, N);
	}

	public byte[] padTag(byte[] tagBytes) {
		// We need the extra byte because BigInteger uses two's complement so it
		// can represent negative values.
		assert (tagBytes.length <= len_N + 1);
		byte[] tagBytesPadded = new byte[len_N + 1];
		System.arraycopy(tagBytes, 0, tagBytesPadded, len_N + 1 - tagBytes.length, tagBytes.length);
		return tagBytesPadded;
	}

	public void tagAll2(OutputStream out) throws IOException {
		in.reset();
		byte[] currentChunk = new byte[in.chunkSize];
		for (int i = 0; i < in.fileChunks; i++) {
			in.readChunk(currentChunk);
			BigInteger tagInt = tagChunk2(currentChunk, i);
			assert (tagInt.compareTo(BigInteger.ZERO) >= 0);
			byte[] tagBytes = padTag(tagInt.toByteArray());
			out.write(tagBytes);

			test_tags.add(tagInt);
		}
	}
	
	public void tagAll(OutputStream out) throws IOException {
		in.reset();
		byte[] currentChunk = new byte[in.chunkSize];
		for (int i = 0; i < in.fileChunks; i++) {
			in.readChunk(currentChunk);
			BigInteger tagInt = tagChunk(currentChunk, i);
			assert (tagInt.compareTo(BigInteger.ZERO) >= 0);
			byte[] tagBytes = padTag(tagInt.toByteArray());
			out.write(tagBytes);

			test_tags.add(tagInt);
		}
	}

	public static void printBigInteger(BigInteger num, PrintStream out) {
		out.println(num.toString(16));
	}
}
