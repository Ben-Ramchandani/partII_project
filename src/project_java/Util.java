package project_java;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Util {
	private Util() {
		throw new RuntimeException();
	}

	public static byte[] hash(byte[] d) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("No SHA256");
		}
		md.update(d);
		return md.digest();
	}

	public static long leastGreaterPowerOf2(long x) {
		long p = 1;
		while (p < x) {
			p *= 2;
		}
		return p;
	}

	public static long divRoundUp(long x, long y) {
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
}