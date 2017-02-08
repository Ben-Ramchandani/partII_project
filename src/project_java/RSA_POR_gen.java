package project_java;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Random;


public class RSA_POR_gen {
	public static final BigInteger TWO = BigInteger.valueOf(2);
	public static final int len_lambda = 512;
	public static final BigInteger len_N = BigInteger.valueOf(len_lambda * 2);
	public static final BigInteger lambda = TWO.pow(len_lambda-4);
	public static final int len_v = 256;
	public BigInteger[] chunks;
	public BigInteger[] tags;

	public static void printBigInteger(BigInteger num, PrintStream out) {
		out.println(num.toString(16));
	}

	// I'm pretty sure this isn't really necessary as the condition is effectively never triggered for large p, q.
	public static BigInteger findGenerator(BigInteger p, BigInteger q, BigInteger N) {
		BigInteger a;
		Random rand = new Random();
		BigInteger aModp, aModq;
		do {
			a = new BigInteger(len_lambda, rand).mod(N);
			aModp = a.mod(p);
			aModq = a.mod(q);
			aModp.equals(BigInteger.ZERO);
		} while (aModp.equals(BigInteger.ZERO) || aModp.equals(BigInteger.ONE)
				|| aModp.equals(p.subtract(BigInteger.ONE)) || aModq.equals(BigInteger.ZERO)
				|| aModq.equals(BigInteger.ONE) || aModq.equals(q.subtract(BigInteger.ONE)));
		return a.multiply(a).mod(N);
	}

	public static void generate(PrintStream out) {
		Random rand = new Random();

		BigInteger p = BigInteger.probablePrime(len_lambda, rand);
		BigInteger pp = p.subtract(BigInteger.ONE).divide(TWO);
		BigInteger q = BigInteger.probablePrime(len_lambda, rand);
		BigInteger qq = q.subtract(BigInteger.ONE).divide(TWO);
		BigInteger N = p.multiply(q);
		BigInteger ppqq = pp.multiply(qq);

		BigInteger g = findGenerator(p, q, N);
		
		BigInteger e;
		BigInteger d;
		do {
			e = BigInteger.probablePrime(len_lambda, rand);
			d = egcd(e, ppqq);
		} while(d.compareTo(lambda) <= 0 || e.compareTo(lambda) <= 0);
		
		BigInteger v = new BigInteger(len_v, rand);
		
		printBigInteger(N, out);
		printBigInteger(g, out);
		printBigInteger(e, out);
		printBigInteger(d, out);
		printBigInteger(v, out);
		printBigInteger(len_N, out);
		
		System.out.println("p: " + p);
		System.out.println("q: " + q);
		System.out.println("pp: " + pp);
		System.out.println("qq: " + qq);
		System.out.println("p'q': " + ppqq);
		System.out.println("N: " + N);
		System.out.println("g: " + g);
		System.out.println("e: " + e);
		System.out.println("d: " + d);
		System.out.println("v: " + v);
		System.out.println("len_N: " + len_N);
	}

	// Find x, y such that ax + by = 1.
	public static BigInteger egcd(BigInteger a, BigInteger b) {
		BigInteger savea = a;
		BigInteger saveb = b;
		BigInteger lastx = BigInteger.ONE, lasty = BigInteger.ZERO,
				x = BigInteger.ZERO, y = BigInteger.ONE, temp;
		BigInteger q, r;

		while (b.compareTo(BigInteger.ZERO) > 0) {
			q = a.divide(b);
			r = a.subtract(q.multiply(b));
			
			a = b;
			b = r;
			
			temp = x;
			x = lastx.subtract(q.multiply(x));
			lastx = temp;
			
			temp = y;
			y = lasty.subtract(q.multiply(y));
			lasty = temp;
		}

		assert (a.equals(BigInteger.ONE));
		assert (savea.multiply(lastx).add(saveb.multiply(lasty)).equals(BigInteger.ONE));
		return lastx;
	}
}

