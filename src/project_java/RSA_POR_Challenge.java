package project_java;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RSA_POR_Challenge {
	public RSA_POR parent;
	public byte[] chunkKey;
	public byte[] coefficientKey;
	public int numChunksProof;

	public RSA_POR_Challenge(RSA_POR parent, byte[] chunkKey, byte[] aCoefficientkey, int numChunks) {
		this.parent = parent;
		this.chunkKey = chunkKey;
		this.coefficientKey = aCoefficientkey;
		this.numChunksProof = numChunks;
	}

	public RSA_Proof genProofSingle(int chunkIndex, byte[] chunkBytes, byte[] chunkTagBytes) {
		BigInteger a = new BigInteger(Util.HMAC(coefficientKey, Util.toBytes(chunkIndex)));
		BigInteger chunk = new BigInteger(chunkBytes);
		BigInteger chunkTag = new BigInteger(chunkTagBytes);

		BigInteger T = chunkTag.modPow(a, parent.N);
		BigInteger M = chunk.multiply(a);
		return new RSA_Proof(T, M);
	}

	public List<Integer> getChunkSet() {
		List<Integer> res = new ArrayList<Integer>();
		assert (numChunksProof <= parent.in.fileBlocks);
		BigInteger chunkKeyInt = new BigInteger(this.chunkKey);
		for (int i = 0; i < numChunksProof; i++) {
			res.add(chunkKeyInt.mod(BigInteger.valueOf(parent.in.fileBlocks)).intValue());
			chunkKeyInt = Util.hash(chunkKeyInt);
		}
		return res;
	}

	public BigInteger getCoefficient(int chunkIndex) {
		// byte[] a = Util.HMAC(coefficientKey, Util.toBytes(chunkIndex));
		// return new BigInteger(1, a);
		return BigInteger.valueOf(2);
	}

	public RSA_Proof genProof(byte[][] chunkTagBytes) {
		List<Integer> chunkSet = getChunkSet();

		BigInteger T = BigInteger.ONE;
		BigInteger M = BigInteger.ZERO;

		for (int i = 0; i < chunkSet.size(); i++) {
			int chunkIndex = chunkSet.get(i);
			BigInteger chunk = new BigInteger(1, parent.in.getChunk(chunkIndex));
			BigInteger a = getCoefficient(chunkIndex);
			BigInteger chunkTag = new BigInteger(1, chunkTagBytes[chunkIndex]);

			T = T.multiply(chunkTag.modPow(a, parent.N)).mod(parent.N);

			M = M.add(chunk.multiply(a));
		}

		return new RSA_Proof(T, M);
	}

	public RSA_Proof genProof(BlockStream chunkTags) {
		assert (chunkTags.blockSize == parent.len_N + 1);
		List<Integer> chunkSet = getChunkSet();

		BigInteger T = BigInteger.ONE;
		BigInteger M = BigInteger.ZERO;

		for (int i = 0; i < chunkSet.size(); i++) {
			int chunkIndex = chunkSet.get(i);
			BigInteger chunk = new BigInteger(1, parent.in.getChunk(chunkIndex));
			BigInteger a = getCoefficient(chunkIndex);
			BigInteger chunkTag = new BigInteger(chunkTags.getChunk(chunkIndex));
			
			System.out.println(chunkIndex + ": " + chunkTag);

			T = T.multiply(chunkTag.modPow(a, parent.N)).mod(parent.N);

			M = M.add(chunk.multiply(a));
		}

		return new RSA_Proof(T, M);
	}

	// UNUSED
	public static byte[] stripPadding(byte[] paddedTag) {
		int i = 0;
		while(paddedTag[i] == 0) {
			i++;
		}
		byte[] tag = new byte[paddedTag.length - i];
		System.arraycopy(paddedTag, i, tag, 0, tag.length);
		return tag;
	}

	public boolean checkProof(RSA_Proof proof) {
		BigInteger T = proof.T;
		BigInteger M = proof.M;
		List<Integer> chunkSet = getChunkSet();

		BigInteger tau = T.modPow(parent.e, parent.N);
		// System.out.println("tau: " + tau);

		Iterator<Integer> it = chunkSet.iterator();
		while (it.hasNext()) {
			int chunkIndex = it.next();
			BigInteger hW_i = parent.gethW_i(chunkIndex);
			BigInteger a_i = getCoefficient(chunkIndex);
			tau = tau.multiply(hW_i.modPow(a_i.negate(), parent.N)).mod(parent.N);
			// System.out.println("tau: " + tau);
		}

		BigInteger gM = parent.g.modPow(M, parent.N);

		// TODO: check |M| < lambda/2
		return gM.equals(tau);
	}
}
