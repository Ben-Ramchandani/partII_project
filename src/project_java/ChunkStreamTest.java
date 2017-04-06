package project_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

public class ChunkStreamTest {

	public static final String TEST_FILE_PATH = "test_files/";
	public static final String testString = "abcdefghijklmnopqrstuvwxyz";
	public static final int blockSize = 10;

	@Test
	public void testBlockStream() throws IOException {
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print(testString);
		writer.close();

		ChunkStream b = new ChunkStream(Paths.get(TEST_FILE_PATH + "file.txt"), blockSize);
		assertEquals(b.fileSize, testString.length());

		byte[] array = new byte[blockSize];

		b.readChunk(array);
		assertEquals(new String(array, StandardCharsets.UTF_8), testString.substring(0, blockSize));
		b.reset();

		for (int i = 0; (i + 1) * blockSize <= testString.length(); i++) {
			b.readChunk(array);
			assertEquals(new String(array, StandardCharsets.UTF_8),
					testString.substring(i * blockSize, (i + 1) * blockSize));
		}

		b.readChunk(array);
		assertTrue(Arrays.equals(Util.byteCombine("uvwxyz".getBytes(StandardCharsets.UTF_8), new byte[4]), array));

		b.readChunk(array);
		assertTrue(Arrays.equals(new byte[blockSize], array));
	}

	@Test
	public void testSingleBlock() throws IOException {
		PrintWriter writer = new PrintWriter(TEST_FILE_PATH + "file.txt", "UTF-8");
		writer.print("a");
		writer.close();

		ChunkStream b = new ChunkStream(Paths.get(TEST_FILE_PATH + "file.txt"), 3);
		assertEquals(b.fileSize, "a".length());

		byte[] array = new byte[3];
		b.readChunk(array);

		assertTrue(array[0] == 97);
		assertTrue(array[1] == 0);
		assertTrue(array[1] == 0);
	}
}
