package project_java;

import java.io.IOException;

import com.backblaze.erasure.ReedSolomon;

public class ErasureEncodedStream extends AChunkStream {
	public AChunkStream in;
	public final int dataShards;
	public final int parityShards;
	private int currentShard;
	private byte[][] shards;

	/*
	 * This represents a ChunkStream that has been erasure coded such that the file can be recovered even if some of the chunks are lost.
	 * We store the whole file in memory here for the JavaReedSolomon library.
	 */
	public ErasureEncodedStream(AChunkStream in, double dataPortion) throws IOException {
		this.in = in;
		this.chunkSize = in.chunkSize;
		this.fileChunks = (int) Math.ceil(1.0 / dataPortion);
		this.fileSize = this.chunkSize * this.fileChunks;
		this.dataShards = in.fileChunks;
		this.parityShards = this.fileChunks - in.fileChunks;
		this.shards = new byte[this.fileChunks][];
		for (int i = 0; i < in.fileChunks; i++) {
			byte[] chunk = new byte[this.chunkSize];
			in.readChunk(chunk);
			shards[i] = chunk;
		}
		ReedSolomon reedSolomon = ReedSolomon.create(this.dataShards, this.parityShards);
		reedSolomon.encodeParity(shards, 0, this.chunkSize);
		this.currentShard = 0;
	}

	@Override
	public void readChunk(byte[] array) throws IOException {
		assert (array.length == this.chunkSize);
		System.arraycopy(this.shards[this.currentShard], 0, array, 0, this.chunkSize);
		this.currentShard += 1;
	}

	@Override
	public void reset() throws IOException {
		this.currentShard = 0;
	}

	@Override
	public byte[] getChunk(long i) throws IOException {
		byte[] chunk = new byte[this.chunkSize];
		System.arraycopy(this.shards[(int) i], 0, chunk, 0, this.chunkSize);
		return chunk;
	}
}