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
		byte[] a = Util.HMAC(coefficientKey, Util.toBytes(chunkIndex));
		return new BigInteger(1, a);
	}

	public RSA_Proof genProof(ChunkStream chunkTags) {
		assert (chunkTags.chunkSize == parent.len_N + 1);
		List<Integer> chunkSet = getChunkSet();

		BigInteger T = BigInteger.ONE;
		BigInteger M = BigInteger.ZERO;

		// T = T_0^(a_0) * T_1^(a_1) ...
		// M = a_0*b_0 + a_1*b_1 ...
		// (selected chunks only)
		for (int i = 0; i < chunkSet.size(); i++) {
			int chunkIndex = chunkSet.get(i);
			BigInteger chunk = new BigInteger(1, parent.in.getChunk(chunkIndex));
			BigInteger a = getCoefficient(chunkIndex);
			BigInteger chunkTag = new BigInteger(chunkTags.getChunk(chunkIndex));

			T = T.multiply(chunkTag.modPow(a, parent.N)).mod(parent.N);

			M = M.add(chunk.multiply(a));
		}

		return new RSA_Proof(T, M);
	}

	public boolean checkProof(RSA_Proof proof) {
		BigInteger T = proof.T;
		BigInteger M = proof.M;
		List<Integer> chunkSet = getChunkSet();

		// tau = T^e
		BigInteger tau = T.modPow(parent.e, parent.N);

		// tau = tau/(h(W_i)^(a_i)) for each i in chunkSet.
		Iterator<Integer> it = chunkSet.iterator();
		while (it.hasNext()) {
			int chunkIndex = it.next();
			BigInteger hW_i = parent.gethW_i(chunkIndex);
			BigInteger a_i = getCoefficient(chunkIndex);
			tau = tau.multiply(hW_i.modPow(a_i.negate(), parent.N)).mod(parent.N);
		}

		// We check g^M == tau
		BigInteger gM = parent.g.modPow(M, parent.N);

		assert (BigInteger.valueOf(M.bitLength()).compareTo(RSA_POR_gen.lambda) < 0);
		return gM.equals(tau);
	}
}
