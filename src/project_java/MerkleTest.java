package project_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.junit.BeforeClass;
import org.junit.Test;

public class MerkleTest {

	public static final String TEST_FILE_PATH = "test_files/";
	public BlockStream b;

	@BeforeClass
	public static void setUp() {
		(new File(TEST_FILE_PATH)).mkdir();
	}

	@Test
	public void testMerkle() throws Exception {
		String testString = "abcdefghijklmnopqrstuvwxyz";
		int blockSize = 10;
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), blockSize);

		Merkle m = new Merkle(this.b);

		assertEquals(m.blockSize, blockSize);
		assertTrue(m.depth == 2);
		assertTrue(m.fileBlocks == 3);
		assertTrue(m.totalBlocks == 4);
	}

	@Test
	public void testLeaf() throws Exception {
		String testString = "a";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 1);
		Merkle m = new Merkle(this.b);

		assertTrue(m.fileBlocks == 1 && m.totalBlocks == 1 && m.depth == 0);
		// KECCAK256 value is from
		// https://emn178.github.io/online-tools/keccak_256.html
		assertTrue(Arrays.equals(m.rootHash(),
				DatatypeConverter.parseHexBinary("3ac225168df54212a25c1c01fd35bebfea408fdac2e31ddd6f80a4bbf9a5f1cb")));
	}

	@Test
	public void testTwoBlocks() throws Exception {
		String testString = "abcde";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 3);
		Merkle m = new Merkle(this.b);

		assertTrue(m.fileBlocks == 2 && m.totalBlocks == 2 && m.depth == 1);
		byte[] firstBlock = "abc".getBytes();
		byte[] secondBlock = Util.byteCombine("de".getBytes(), new byte[1]);
		byte[] manualHash = Util.hash(Util.byteCombine(Util.hash(firstBlock), Util.hash(secondBlock)));
		assertTrue(Arrays.equals(m.rootHash(), manualHash));
	}

	@Test
	public void testLargerTree() throws Exception {
		String testString = "abcdefghijklmnopqrstuvwxyz";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 2);
		Merkle m = new Merkle(this.b);
		m.rootHash();
		m.proof(0);
		m.proof(4);
		m.proof(12);

		// Just a sanity check.
		assertTrue(m.fileBlocks == 13 && m.totalBlocks == 16 && m.depth == 4);
	}

	@Test
	public void testProof() throws Exception {
		String testString = "a";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 1);
		Merkle m = new Merkle(this.b);
		assertTrue(Arrays.equals(m.proof(0), "a".getBytes()));
	}

	@Test
	public void testSmallProof() throws Exception {
		String testString = "ab";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 1);
		Merkle m = new Merkle(this.b);
		assertTrue(Arrays.equals(m.proof(0), Util.byteCombine("a".getBytes(), Util.hash("b".getBytes()))));
	}

	@Test
	public void testMediumProof() throws Exception {
		String testString = "abcde";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 2);
		Merkle m = new Merkle(this.b);
		assertTrue(m.fileBlocks == 3 && m.totalBlocks == 4 && m.depth == 2);

		// Hash for ab^cd
		byte[] hashLeft = Util.hash(Util.byteCombine(Util.hash("ab".getBytes()), Util.hash("cd".getBytes())));
		// Hash for e_^__ where _ is the zero byte.
		byte[] hashRight = Util.hash(
				Util.byteCombine(Util.hash(Util.byteCombine("e".getBytes(), new byte[1])), Util.hash(new byte[2])));
		byte[] manualProof0 = Util.byteCombine("ab".getBytes(), Util.hash("cd".getBytes()), hashRight);
		assertTrue(Arrays.equals(m.proof(0), manualProof0));
		byte[] manualProof1 = Util.byteCombine("cd".getBytes(), Util.hash("ab".getBytes()), hashRight);
		assertTrue(Arrays.equals(m.proof(1), manualProof1));
		byte[] manualProof2 = Util.byteCombine(Util.byteCombine("e".getBytes(), new byte[1]), Util.hash(new byte[2]),
				hashLeft);
		assertTrue(Arrays.equals(m.proof(2), manualProof2));
		byte[] manualProof3 = Util.byteCombine(new byte[2], Util.hash(Util.byteCombine("e".getBytes(), new byte[1])),
				hashLeft);
		assertTrue(Arrays.equals(m.proof(3), manualProof3));
	}

	@Test
	public void testValidate() throws Exception {
		String testString = "a";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 1);
		Merkle m = new Merkle(this.b);
		byte[] proof = m.proof(0);
		byte[] rootHash = m.rootHash();
		assertTrue(m.validateProof(rootHash, proof, 0));
		proof[0] = "b".getBytes()[0];
		assertFalse(m.validateProof(rootHash, proof, 0));
	}

	@Test
	public void testSmallValidate() throws Exception {
		String testString = "ab";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 1);
		Merkle m = new Merkle(this.b);
		byte[] proof = m.proof(0);
		byte[] rootHash = m.rootHash();
		assertTrue(m.validateProof(rootHash, proof, 0));
		proof[32]++;
		assertFalse(m.validateProof(rootHash, proof, 0));
		proof = m.proof(1);
		assertTrue(m.validateProof(rootHash, proof, 1));
		assertFalse(m.validateProof(rootHash, proof, 0));
	}

	@Test
	public void testLargeValidate() throws Exception {
		String testString = "abcdefghijklmnopqrstuvwxyz";
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "file.txt"), 3);
		Merkle m = new Merkle(this.b);
		byte[] proof = m.proof(0);
		byte[] rootHash = m.rootHash();
		for (int i = 0; i < m.fileBlocks; i++) {
			proof = m.proof(i);
			for (int j = 0; j < m.fileBlocks; j++) {
				if (i == j) {
					assertTrue(m.validateProof(rootHash, proof, j));
				} else {
					assertFalse(m.validateProof(rootHash, proof, j));
				}
			}
		}
	}

	@Test
	public void testBigFile() throws Exception {
		this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "big.file"), 1024);
		// //This takes 36 seconds to run, so is left disabled.
		// //It ends up taking about 16 seconds per gigabyte.
		// Merkle m = new Merkle(this.b);
		// System.out.println(m.toString());
		// System.out.println(DatatypeConverter.printHexBinary(m.rootHash()));
		// System.out.println(DatatypeConverter.printHexBinary(m.proof(93280)));
	}

	@Test
	public void testBigFileValidate() throws Exception {
		// This takes about 60 seconds.
		// int blockSize = 1024;
		// this.b = new BlockStream(Paths.get(TEST_FILE_PATH + "big.file"),
		// blockSize);
		// Merkle m = new Merkle(this.b);
		// int blockNumber = 4;
		// byte[] proof = m.proof(blockNumber);
		// byte[] rootHash = m.rootHash();
		// assertTrue(Merkle.validate(1024, rootHash, proof, blockNumber));
		// assertFalse(Merkle.validate(1024, rootHash, proof, blockNumber + 1));
		// proof[1727]++;
		// assertFalse(Merkle.validate(1024, rootHash, proof, blockNumber));
		// proof[1727]--;
		// assertTrue(Merkle.validate(1024, rootHash, proof, blockNumber));
		// rootHash[13]++;
		// assertFalse(Merkle.validate(1024, rootHash, proof, blockNumber));
	}

}
