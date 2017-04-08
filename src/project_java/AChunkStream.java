package project_java;

import java.io.IOException;

public abstract class AChunkStream {
	
	public int chunkSize;
	public long fileSize;
	public long fileChunks;
	
	public abstract void readChunk(byte[] array) throws IOException;

	public abstract void reset() throws IOException;

	public abstract byte[] getChunk(long i) throws IOException;
}
