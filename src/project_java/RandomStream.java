package project_java;

import java.io.IOException;
import java.util.Random;

public class RandomStream extends AChunkStream {
	
	Random r;

	public RandomStream(int fileSize, int blockLen) throws IOException {
		this.chunkSize = blockLen;
		this.fileSize = fileSize;
		this.fileChunks = Util.divRoundUp(fileSize, this.chunkSize);
		this.r = new Random(1);
	}

	public void readChunk(byte[] array) throws IOException {
		assert (array.length == this.chunkSize);
		r.nextBytes(array);
	}

	public void reset() throws IOException {}

	public byte[] getChunk(long i) throws IOException {
		assert (i < fileChunks);
		byte[] array = new byte[chunkSize];
		r.nextBytes(array);
		return array;
	}
}
