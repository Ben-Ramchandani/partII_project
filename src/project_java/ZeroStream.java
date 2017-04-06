package project_java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ZeroStream extends AChunkStream {
	private final byte[] zeroArray;

	private SeekableByteChannel in;

	public ZeroStream(int fileSize, int blockLen) throws IOException {
		this.in = Files.newByteChannel(Paths.get("/dev/zero"));
		this.chunkSize = blockLen;
		this.fileSize = fileSize;
		this.zeroArray = new byte[blockLen];
		this.fileChunks = Util.divRoundUp(fileSize, this.chunkSize);
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

	public byte[] getChunk(long i) throws IOException {
		assert (i < fileChunks);
		in.position(i * chunkSize);
		ByteBuffer b = ByteBuffer.wrap(new byte[chunkSize]);

		while (b.position() < this.chunkSize && this.in.position() < this.fileSize) {
			in.read(b);
		}

		if (b.position() < chunkSize) {
			b.put(this.zeroArray, 0, chunkSize - b.position());
		}
		return b.array();
	}
}
