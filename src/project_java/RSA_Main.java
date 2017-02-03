package project_java;

import java.io.File;
import java.nio.file.Paths;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		System.out.println("Hi");
		//PrintStream p = new PrintStream("test_files/keys.txt");
		//RSA_POR_gen.generate(p);
		File f = new File("test_files/keys.txt");
		BlockStream bl = new BlockStream(Paths.get("test_files/file.txt"), Main.blockSize);
		RSA_POR r = new RSA_POR(f, bl);
		byte[] b = new byte[3];
		b[0] = 1; b[1] = 2; b[2] = 3;
		RSA_POR.printBigInteger(r.tagChunk(b, 0), System.out);
	}

}
