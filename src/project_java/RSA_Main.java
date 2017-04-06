package project_java;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

public class RSA_Main {

	public static void main(String[] args) throws Throwable {
		// test2();
		// return;

		// System.out.println("Hi");
		// for (int i = 0; i < 1; i++) {
		// test();
		// }
	}

	public static void test2() throws Throwable {
		ChunkStream bl = new ChunkStream(Paths.get("test_files/pic.png"), Main.chunkSize);
		System.out.println("BlockSize: " + bl.chunkSize);
		System.out.println("FileBlocks: " + bl.fileChunks);
		RSA_POR r = new RSA_POR("test_files/pic.png.keys", bl, 1);

		byte[] blockHash = DatatypeConverter
				.parseHexBinary("a4f80a26aecb5fc975e12a9d3d0f6a7907c60b1d7e6b5205dfb4c984dad7f1ba");

		ChunkStream tags = new ChunkStream(Paths.get("test_files/pic.png.tags"), r.len_N + 1);

		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, blockHash);
		System.out.println("Chunks: " + chal.getChunkSet());

		RSA_Proof proof = chal.genProof(tags);

		boolean succ = chal.checkProof2(proof);
		System.out.println(succ);
		if (!succ) {
			throw new RuntimeException();
		}
	}

	public static void test() throws Throwable {
		// PrintStream p = new PrintStream("test_files/keys.txt");
		// BigInteger privatekey = RSA_POR_gen.generate(p, null);
		// p.close();

		ChunkStream bl = new ChunkStream(Paths.get("test_files/pic.png"), Main.chunkSizeRSA);
		System.out.println("BlockSize: " + bl.chunkSize);
		System.out.println("FileBlocks: " + bl.fileChunks);
		RSA_POR r = new RSA_POR("test_files/pic.png.keys", bl, 1);

		BigInteger privateKey = new BigInteger("2d9825406afa991f3adbced720ae7f8aabd6d858936677bc9e340f0d332a1e05", 16);

		r.tagAll(new FileOutputStream(new File("test_files/pic.png.tags")), privateKey);

		// Random rand = new Random();
		// byte[] blockHash = new byte[256];
		// rand.nextBytes(blockHash);
		byte[] blockHash = DatatypeConverter
				.parseHexBinary("a4f80a26aecb5fc975e12a9d3d0f6a7907c60b1d7e6b5205dfb4c984dad7f1ba");

		System.out.println(Main.getProofChunks(blockHash, 1, bl.fileChunks));

		ChunkStream tags = new ChunkStream(Paths.get("test_files/pic.png.tags"), r.len_N + 1);

		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, blockHash);
		System.out.println("Chunks: " + chal.getChunkSet());

		RSA_Proof proof = chal.genProof(tags);

		boolean succ = chal.checkProof2(proof);
		System.out.println(succ);
		if (!succ) {
			throw new RuntimeException();
		}
	}
}
