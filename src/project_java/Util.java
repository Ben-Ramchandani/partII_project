package project_java;

import java.io.IOException;
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

	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
}
