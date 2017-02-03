package project_java;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Random;


public class RSA_POR_gen {
	public static final BigInteger TWO = BigInteger.ONE.add(BigInteger.ONE);
	public static final int len_lambda = 512;
	public static final BigInteger lambda = TWO.pow(len_lambda-5);
	public static final int len_v = 256;
	public BigInteger[] chunks;
	public BigInteger[] tags;

	public static void printBigInteger(BigInteger num, PrintStream out) {
		out.println(num.toString(16));
	}

	// I'm pretty sure this isn't really necessary as the condition is effectively never triggered
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
		BigInteger qq = p.subtract(BigInteger.ONE).divide(TWO);
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
		
		printBigInteger(p, out);
		printBigInteger(q, out);
		printBigInteger(N, out);
		printBigInteger(g, out);
		printBigInteger(e, out);
		printBigInteger(d, out);
		printBigInteger(v, out);
	}

	// Find x, y such that ax + by = 1.
	public static BigInteger egcd(BigInteger a, BigInteger b) {
		BigInteger x_1 = BigInteger.ZERO, x_2 = BigInteger.ONE, y_1 = BigInteger.ONE, y_2 = BigInteger.ZERO,
				x = BigInteger.ONE, y = BigInteger.ZERO;
		BigInteger q, r;

		while (b.compareTo(BigInteger.ZERO) > 0) {
			q = a.divide(b);
			r = a.subtract(q.multiply(b));
			x = x_2.subtract(q.multiply(x_1));
			y = y_2.subtract(q.multiply(y_1));
			a = b;
			b = r;
			x_2 = x_1;
			y_2 = y_1;
			x_1 = x;
			y_1 = y;
		}

		assert (a.equals(BigInteger.ONE));
		assert (a.multiply(x).add(b.multiply(y)).equals(BigInteger.ONE));
		return x;
	}
}

