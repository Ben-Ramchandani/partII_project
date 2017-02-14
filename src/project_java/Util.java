package project_java;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.cryptohash.Keccak256;

public final class Util {
	private Util() {
		throw new RuntimeException();
	}

	public static byte[] hash(byte[] d) {
		Keccak256 kek = new Keccak256();
		kek.update(d);
		return kek.digest();
	}
	
	public static BigInteger hash(BigInteger d) {
		return new BigInteger(Util.hash(d.toByteArray()));
	}
	
	public static byte[] HMAC(byte[] key, byte[] m) {
		// We can just concatenate the key with the message as KECCAK-256 is secure against length extension attacks (http://keccak.noekeon.org/).
		return Util.hash(Util.byteCombine(key, m));
	}
	
	public static byte[] toBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.putInt(x);
	    return buffer.array();
	}
	
	public static byte[] toBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static long leastGreaterPowerOf2(long x) {
		long p = 1;
		while (p < x) {
			p *= 2;
		}
		return p;
	}

	public static int divRoundUp(int x, int y) {
		if (x % y == 0) {
			return x / y;
		} else {
			return (x / y) + 1;
		}
	}

	public static int log2(long x) {
		return 63 - Long.numberOfLeadingZeros(x);
	}

	public static byte[] byteCombine(byte[] a, byte[] b) {
		int length = a.length + b.length;
		byte[] result = new byte[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static byte[] byteCombine(byte[] a, byte[] b, byte[] c) {
		return Util.byteCombine(Util.byteCombine(a, b), c);
	}

	public static byte[] slice(byte[] in, int from, int to) {
		int len = to - from;
		byte[] res = new byte[len];
		System.arraycopy(in, from, res, 0, len);
		return res;
	}

	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
