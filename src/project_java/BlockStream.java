package project_java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlockStream {
	private final byte[] zeroArray;
	
	private SeekableByteChannel in;
	public final int blockSize;
	public final long fileSize;
	public final long fileBlocks;
	
	public BlockStream(Path p, int blockLen) throws IOException {
		this.in = Files.newByteChannel(p);
		this.blockSize = blockLen;
		this.fileSize = in.size();
		this.zeroArray = new byte[blockLen];
		this.fileBlocks = Util.divRoundUp(fileSize, (long) this.blockSize);
	}
	
	public void readBlock(byte[] array) throws IOException {
		assert(array.length == this.blockSize);
		ByteBuffer b = ByteBuffer.wrap(array);
		
		while(b.position() < this.blockSize && this.in.position() < this.fileSize) {
			in.read(b);
		}
		
		if(b.position() < blockSize) {
			b.put(this.zeroArray, 0, blockSize - b.position());
		}
	}
	
	public void reset() throws IOException {
		this.in.position(0);
	}
	
	//TODO: get chunk at index i
	public byte[] getChunk(long i) {
		return null;
	}
}
