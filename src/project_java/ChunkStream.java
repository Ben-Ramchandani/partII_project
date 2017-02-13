package project_java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChunkStream {
	private final byte[] zeroArray;

	private SeekableByteChannel in;
	public final int chunkSize;
	public final long fileSize;
	public final long fileBlocks;

	public ChunkStream(Path p, int blockLen) throws IOException {
		this.in = Files.newByteChannel(p);
		this.chunkSize = blockLen;
		this.fileSize = in.size();
		this.zeroArray = new byte[blockLen];
		this.fileBlocks = Util.divRoundUp(fileSize, (long) this.chunkSize);
	}

	public void readChunk(byte[] array) throws IOException {
		assert (array.length == this.chunkSize);
		ByteBuffer b = ByteBuffer.wrap(array);

		while (b.position() < this.chunkSize && this.in.position() < this.fileSize) {
			in.read(b);
		}

		if (b.position() < chunkSize) {
			b.put(this.zeroArray, 0, chunkSize - b.position());
		}
	}

	public void reset() throws IOException {
		this.in.position(0);
	}

	public byte[] getChunk(long i) {
		assert(i < fileBlocks);
		try {
			in.position(i * chunkSize);
			ByteBuffer b = ByteBuffer.wrap(new byte[chunkSize]);
			
			while (b.position() < this.chunkSize && this.in.position() < this.fileSize) {
				in.read(b);
			}
			
			if (b.position() < chunkSize) {
				b.put(this.zeroArray, 0, chunkSize - b.position());
			}
			return b.array();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
