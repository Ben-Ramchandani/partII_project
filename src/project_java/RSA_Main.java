package project_java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Random;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		exampleRun();
	}

	public static void exampleRun() throws Throwable {
		PrintStream p = new PrintStream("test_files/pic.png.keys");
		BigInteger privateKey = RSA_POR_gen.generate(p, null);
		p.close();

		ChunkStream bl = new ChunkStream(Paths.get("test_files/pic.png"), Main.chunkSizeRSA);
		RSA_POR r = new RSA_POR("test_files/pic.png.keys", bl, 1);

		r.tagAll(new FileOutputStream(new File("test_files/pic.png.tags")), privateKey);

		Random rand = new Random();
		byte[] blockHash = new byte[256];
		rand.nextBytes(blockHash);

		ChunkStream tags = new ChunkStream(Paths.get("test_files/pic.png.tags"), r.len_N + 1);

		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, blockHash);

		RSA_Proof proof = chal.genProof(tags);

		boolean succ = chal.checkProof(proof);
		System.out.println(succ);
		if (!succ) {
			throw new RuntimeException();
		}
	}
}
