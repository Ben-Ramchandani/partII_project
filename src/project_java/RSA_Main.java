package project_java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Random;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		byte a = 1;
		byte b = 20;
		System.out.println((byte)(a ^ b));
		
		
		System.out.println("Hi");
		for (int i = 0; i < 10; i++) {
			test();
		}
	}

	public static void test() throws Throwable {
		PrintStream p = new PrintStream("test_files/keys.txt");
		RSA_POR_gen.generate(p);
		p.close();

		File f = new File("test_files/keys.txt");
		ChunkStream bl = new ChunkStream(Paths.get("test_files/pic.png"), 1024);
		System.out.println("BlockSize: " + bl.chunkSize);
		System.out.println("FileBlocks: " + bl.fileChunks);
		RSA_POR r = new RSA_POR(f, bl);
		r.tagAll2(new FileOutputStream(new File("test_files/out.tags")));

		Random rand = new Random();
		byte[] coefficientKey = new byte[256];
		rand.nextBytes(coefficientKey);
		byte[] chunkKey = new byte[256];
		rand.nextBytes(chunkKey);

		ChunkStream tags = new ChunkStream(Paths.get("test_files/out.tags"), r.len_N + 1);

		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, chunkKey, coefficientKey, 10);
		System.out.println("Chunks: " + chal.getChunkSet());

		RSA_Proof proof = chal.genProof(tags);

		boolean succ = chal.checkProof2(proof);
		System.out.println(succ);
		if (!succ) {
			throw new RuntimeException();
		}
	}
}
