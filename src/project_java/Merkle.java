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
		this.proofLength = this.blockSize + this.depth * Merkle.hashLength;
	}

	private BlockStream in;
	private byte[] bytes;
	public final int blockSize;
	public final int depth;
	public final long fileBlocks;
	public final long totalBlocks;
	public static final int hashLength = 32; // 256 bits
	public final int proofLength;
	
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

	public byte[] proof(long i) throws IOException {
		assert (i >= 0 && i < this.fileBlocks) : "Proof block does not exist.";
		this.in.reset();
		byte[] proof = this.proofr(this.depth, i);
		assert (proof.length == this.blockSize + Merkle.hashLength * this.depth);
		return proof;
	}

	private byte[] proofr(int depth, long i) throws IOException {
		if (depth == 0) {
			byte[] block = new byte[this.blockSize];
			this.in.readBlock(block);
			return block;
		} else {
			depth -= 1;
			long M = (1L << depth); // = 2^(depth)
			if (i < M) {
				return Util.byteCombine(this.proofr(depth, i), this.merkler(depth));
			} else {
				byte[] tmp = this.merkler(depth);
				return Util.byteCombine(this.proofr(depth, i - M), tmp);
			}
		}
	}

	public boolean validateProof(byte[] rootHash, byte[] proof, int i) {
		if (proof.length != this.blockSize + Merkle.hashLength * this.depth) {
			throw new RuntimeException("Assertion failed, proof length inconsistent.");
		}
		return Merkle.validate(this.blockSize, rootHash, proof, i);
	}

	public static boolean validate(int blockSize, byte[] rootHash, byte[] proof, int i) {
		/*
		 * Proof:
		 * [FILE_CHUNK[i] ######] | [KECCAK256] | [KECCAK256'] | ...
		 * \                    /   \                              /
		 *       Chunk size                depth * hash length
		 * 
		 *                        ^
		 *              Initial proof position
		 * 
		 * Initial value of hash is KECCCAK256(FILE_CHUNK[i]).
		 * 
		 * We walk up the Merkle tree.
		 * At each point we take the next hash from the proof (otherHash),
		 * and work out whether we're coming up a right branch or a left branch (which depends on i).
		 * 
		 * A proof is valid iff it results in the root hash.
		 */
		
		int depth = (proof.length - blockSize) / Merkle.hashLength;
		byte[] hash = Util.hash(Util.slice(proof, 0, blockSize));
		int proofPosition = blockSize;
		
		
		for (int n = 0; n < depth; n++) {
			byte[] otherHash = Util.slice(proof, proofPosition, proofPosition + Merkle.hashLength);
			if (i % 2 == 0) { // We have the left hash.
				hash = Util.hash(Util.byteCombine(hash, otherHash));
			} else {
				hash = Util.hash(Util.byteCombine(otherHash, hash));
			}
			i /= 2;
			proofPosition += Merkle.hashLength;
		}
		return Arrays.equals(hash, rootHash);
	}

	@Override
	public String toString() {
		return "Merkle tree." + "\nBlock size: " + this.blockSize + "\nFile blocks: " + this.fileBlocks
				+ "\nTotal blocks : " + this.totalBlocks + "\nTree depth: " + this.depth;
	}
}
