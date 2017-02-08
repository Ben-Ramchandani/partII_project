package project_java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		PrintStream p = new PrintStream("test_files/keys.txt");
		RSA_POR_gen.generate(p);
		p.close();
		System.out.println("Hi");
		
		File f = new File("test_files/keys.txt");
		BlockStream bl = new BlockStream(Paths.get("test_files/pic.png"), 2048);
		System.out.println("BlockSize: " + bl.blockSize);
		System.out.println("FileBlocks: " + bl.fileBlocks);
		RSA_POR r = new RSA_POR(f, bl);
		r.tagAll(new FileOutputStream(new File("test_files/out.tags")));
		//System.exit(0);
		
		byte[] key = new byte[1];
		byte[] key2 = new byte[1];
		
		key2[0] = 3;
		BlockStream tags = new BlockStream(Paths.get("test_files/out.tags"), r.len_N + 1);
		
		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, key2, key, 4);
		System.out.println("Chunks: " + chal.getChunkSet());
		
		System.out.println("Len N: " + r.len_N);
		System.out.println("Tags BlockSize: " + tags.blockSize);
		System.out.println("Tags FileBlocks: " + tags.fileBlocks);
		System.out.println();
		RSA_Proof proof = chal.genProof(tags);
		System.out.println("T: " + proof.T + "; M: " + proof.M.signum());
		
		System.out.println(chal.checkProof(proof));
	}

}
