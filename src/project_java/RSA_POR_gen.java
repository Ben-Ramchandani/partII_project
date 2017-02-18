package project_java;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class RSA_POR_gen {
	public static final BigInteger TWO = BigInteger.valueOf(2);
	public static final int len_lambda = 128;
	public static final BigInteger len_N = BigInteger.valueOf(len_lambda * 2);
	public static final BigInteger lambda = TWO.pow(len_lambda - 4);
	public static final int len_v = 256;
	public static final int len_v_bytes = len_v / 8;
	public static final boolean printValues = false;
	public BigInteger[] chunks;
	public BigInteger[] tags;

	public static void printBigInteger(BigInteger num, PrintStream out) {
		out.println(num.toString(16));
	}

	// This isn't really necessary for large p, q as the change of the loop
	// repeating is tiny.
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

	// TODO: put public and private parts in different files.
	public static BigInteger generate(PrintStream out, PrintStream privateKeyOut) {
		Random rand = new Random();

		BigInteger p = BigInteger.probablePrime(len_lambda, rand);
		// BigInteger pp = p.subtract(BigInteger.ONE).divide(TWO);
		BigInteger pp = p.subtract(BigInteger.ONE);

		BigInteger q = BigInteger.probablePrime(len_lambda, rand);
		// BigInteger qq = q.subtract(BigInteger.ONE).divide(TWO);
		BigInteger qq = q.subtract(BigInteger.ONE);

		BigInteger N = p.multiply(q);
		BigInteger ppqq = pp.multiply(qq);

		BigInteger g = findGenerator(p, q, N);

		BigInteger e;
		BigInteger d;
		do {
			e = BigInteger.probablePrime(len_lambda, rand);
			d = egcd(e, ppqq);
		} while (d.compareTo(lambda) <= 0 || e.compareTo(lambda) <= 0);

		BigInteger v = new BigInteger(len_v, rand);

		assert (e.multiply(d).mod(ppqq).equals(BigInteger.ONE));
		ArrayList<Long> test_list = new ArrayList<Long>();
		test_list.add((long) 5);
		test_list.add((long) 72359823);
		test_list.add((long) 859335);
		for (int i = 0; i < test_list.size(); i++) {
			BigInteger test_val = BigInteger.valueOf(test_list.get(i));
			assert (test_val.modPow(e, N).modPow(d, N).equals(test_val));
		}

		printBigInteger(N, out);
		printBigInteger(g, out);
		printBigInteger(e, out);
		printBigInteger(v, out);
		printBigInteger(len_N, out);

		if (privateKeyOut != null) {
			printBigInteger(d, privateKeyOut);
		}

		if (printValues) {
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
		
		return d;
	}

	// Find x, y such that ax + by = 1.
	public static BigInteger egcd(BigInteger a, BigInteger b) {
		BigInteger savea = a;
		BigInteger saveb = b;
		BigInteger lastx = BigInteger.ONE, lasty = BigInteger.ZERO, x = BigInteger.ZERO, y = BigInteger.ONE, temp;
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
