package project_java;

import java.io.IOException;
import java.util.Random;

public class RandomStream extends AChunkStream {
	
	Random rand;

	public RandomStream(long fileSize, int blockLen) throws IOException {
		this.chunkSize = blockLen;
		this.fileSize = fileSize;
		this.fileChunks = Util.divRoundUp(fileSize, this.chunkSize);
		this.rand = new Random(1);
	}

	public void readChunk(byte[] array) throws IOException {
		assert (array.length == this.chunkSize);
		rand.nextBytes(array);
	}

	public void reset() throws IOException {
		this.rand = new Random(1);
	}

	public byte[] getChunk(long i) throws IOException {
		assert (i < fileChunks);
		byte[] array = new byte[chunkSize];
		Random tempRand = new Random(i);
		tempRand.nextBytes(array);
		return array;
	}
}
