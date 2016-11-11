package project_java;

import java.io.IOException;
import java.util.Arrays;

public final class Merkle {
	public Merkle(BlockStream in) {
		this.in = in;
		this.blockSize = in.blockSize;
		this.bytes = new byte[this.blockSize];
		this.fileBlocks = Util.divRoundUp(in.fileSize, (long) this.blockSize);
		this.totalBlocks = Util.leastGreaterPowerOf2(this.fileBlocks);
		this.depth = Util.log2(this.totalBlocks);
	}

	private BlockStream in;
	private byte[] bytes;
	public final int blockSize;
	public final int depth;
	public final long fileBlocks;
	public final long totalBlocks;
	public final int hashLength = 32; // 256 bits

	public byte[] rootHash() throws IOException {
		this.in.reset();
		return this.merkler(this.depth);
	}

	private byte[] merkler(int depth) throws IOException {
		if (depth == 0) {
			this.in.readBlock(this.bytes);
			return Util.hash(this.bytes);
		} else {
			return Util.hash(Util.byteCombine(this.merkler(depth - 1), this.merkler(depth - 1)));
		}
	}

	public byte[] proof(int i) throws IOException {
		assert (i >= 0 && i < this.fileBlocks);
		this.in.reset();
		byte[] proof = this.proofr(this.depth, this.totalBlocks, i);
		assert (proof.length == this.blockSize + this.hashLength * this.depth);
		return proof;
	}

	private byte[] proofr(int depth, long N, int i) throws IOException {
		assert (Util.log2(N) == depth);
		long M = N / 2;
		if (M == 0) {
			byte[] block = new byte[this.blockSize];
			this.in.readBlock(block);
			return block;
		} else {
			if (i < M) {
				return Util.byteCombine(this.proofr(depth - 1, M, i), this.merkler(depth - 1));
			} else {
				byte[] tmp = this.merkler(depth - 1);
				return Util.byteCombine(this.proofr(depth - 1, M, (int) (i - M)), tmp);
			}
		}
	}

	public boolean validateProof(byte[] rootHash, byte[] proof, int i) {
		assert (proof.length == this.blockSize + this.hashLength * this.depth);
		byte[] hash = Util.hash(Util.slice(proof, 0, this.blockSize));

		int proofPosition = this.blockSize;
		for (int n = 0; n < this.depth; n++) {
			byte[] otherHash = Util.slice(proof, proofPosition, proofPosition + this.hashLength);
			if (i % 2 == 0) { // We have the left hash.
				hash = Util.hash(Util.byteCombine(hash, otherHash));
			} else {
				hash = Util.hash(Util.byteCombine(otherHash, hash));
			}
			i /= 2;
			proofPosition += this.hashLength;
		}
		return Arrays.equals(hash, rootHash);
	}
}
