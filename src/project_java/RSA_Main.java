package project_java;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Paths;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		System.out.println(BigInteger.valueOf(4).modPow(BigInteger.valueOf(194), BigInteger.valueOf(35)));

		
		//PrintStream p = new PrintStream("test_files/keys.txt");
		//RSA_POR_gen.generate(p);
		File f = new File("test_files/test_keys.txt");
		BlockStream bl = new BlockStream(Paths.get("test_files/rsa_test.txt"), 1);
		RSA_POR r = new RSA_POR(f, bl);
		BigInteger tag = r.tagChunk(0);
		//System.out.println(bl.getChunk(0)[0]);
		//RSA_POR.printBigInteger(r.tagChunk(b, 0), System.out);
		System.out.println("Tag: " + tag);
		
		byte[] key = new byte[1];
		
		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, key, key, 1);
		//System.out.println(chal.getChunkSet());
		
		byte[][] tags = new byte[1][];
		tags[0] = tag.toByteArray();
		RSA_Proof proof = chal.genProof(tags);
		System.out.println("T: " + proof.T + "; M: " + proof.M);
		
		System.out.println(chal.checkProof(proof));
	}

}
