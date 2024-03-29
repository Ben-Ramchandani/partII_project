package project_java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RSA_POR {

	public static int maxChunksBits = 32;

	public final BigInteger N;
	public final BigInteger g;
	public final BigInteger e;
	public final int len_N;
	public final byte[] v;
	public final int numProofChunks;

	public ArrayList<BigInteger> test_tags = new ArrayList<BigInteger>();

	public AChunkStream in;

	public RSA_POR(String keyFile, AChunkStream in, int numProofChunks) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(keyFile));
		String line;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		N = new BigInteger(lines.remove(0), 16);
		g = new BigInteger(lines.remove(0), 16);
		e = new BigInteger(lines.remove(0), 16);
		v = new byte[RSA_POR_gen.len_v_bytes];
		byte[] vInt = new BigInteger(lines.remove(0), 16).toByteArray();
		if (vInt.length > RSA_POR_gen.len_v_bytes) {
			// Because BigInteger allows for signed numbers it uses two's
			// complement and may put a zero byte on the front.
			assert (vInt.length == 1 + RSA_POR_gen.len_v_bytes && vInt[0] == 0);
			System.arraycopy(vInt, 1, v, 0, RSA_POR_gen.len_v_bytes);
		} else {
			System.arraycopy(vInt, 0, v, RSA_POR_gen.len_v_bytes - vInt.length, vInt.length);
		}
		len_N = Integer.parseInt(lines.remove(0), 16);

		this.in = in;
		this.numProofChunks = numProofChunks;

	}

	public BigInteger gethW_i(int i) {
		return Util.EVM_HMAC(i, v);
	}

	public BigInteger tagChunk(byte[] chunkBytes, int chunkIndex, BigInteger d) {
		BigInteger chunk = new BigInteger(1, chunkBytes);
		BigInteger hW_i = gethW_i(chunkIndex);
		BigInteger hW_i_inverse = hW_i.modInverse(N);
		BigInteger g_to_chunk = g.modPow(chunk, N);
		BigInteger mult = hW_i_inverse.multiply(g_to_chunk);
		assert (chunk.compareTo(BigInteger.ZERO) >= 0);
		return mult.modPow(d, N);
	}

	public byte[] padTag(byte[] tagBytes) {
		// We need the extra byte because BigInteger uses two's complement.
		assert (tagBytes.length <= len_N + 1);
		byte[] tagBytesPadded = new byte[len_N + 1];
		System.arraycopy(tagBytes, 0, tagBytesPadded, len_N + 1 - tagBytes.length, tagBytes.length);
		return tagBytesPadded;
	}

	public void tagAll(OutputStream out, BigInteger d) throws IOException {
		in.reset();

		byte[] currentChunk = new byte[in.chunkSize];
		for (int i = 0; i < in.fileChunks; i++) {
			in.readChunk(currentChunk);
			BigInteger tagInt = tagChunk(currentChunk, i, d);
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
