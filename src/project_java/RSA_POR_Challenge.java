package project_java;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class RSA_POR_Challenge {
	public RSA_POR parent;
	public final byte[] chunkKey;
	public final byte[] coefficientKey;
	public final int numChunksProof;
	public final byte[] blockHash;

	public RSA_POR_Challenge(RSA_POR parent, byte[] blockHash) {
		this.parent = parent;
		this.chunkKey = blockHash;
		this.coefficientKey = Util.hash(blockHash);
		this.numChunksProof = parent.numProofChunks;
		this.blockHash = blockHash;
	}

	public List<Integer> getChunkSet() {
		return Main.getProofChunks(blockHash, numChunksProof, parent.in.fileChunks);
	}

	public BigInteger getCoefficient(int chunkIndex) {
		// The lower 16 bytes of the hash result.
		return Util.EVM_HMAC(chunkIndex, coefficientKey).and(new BigInteger("ffffffffffffffff", 16));
	}

	public RSA_Proof genProof(AChunkStream tags) throws IOException {
		assert (tags.chunkSize == parent.len_N + 1);
		List<Integer> chunkSet = getChunkSet();

		BigInteger T = BigInteger.ONE;
		BigInteger M = BigInteger.ZERO;

		// T = T_0^(a_0) * T_1^(a_1) ...
		// M = a_0*b_0 + a_1*b_1 ...
		// (selected chunks only)
		for (int i = 0; i < chunkSet.size(); i++) {

			int chunkIndex = chunkSet.get(i);
			System.out.println("Generating proof on chunk " + chunkIndex + " (" + (i + 1) + " out of "
					+ chunkSet.size() + ").");
			BigInteger chunk = new BigInteger(1, parent.in.getChunk(chunkIndex));
			BigInteger a = getCoefficient(chunkIndex);
			BigInteger chunkTag = new BigInteger(tags.getChunk(chunkIndex));

			T = T.multiply(chunkTag.modPow(a, parent.N)).mod(parent.N);

			M = M.add(chunk.multiply(a));
		}

		return new RSA_Proof(T, M);
	}

	public boolean checkProof(RSA_Proof proof) {
		BigInteger T = proof.T;
		BigInteger M = proof.M;
		List<Integer> chunkSet = getChunkSet();

		System.out.println("N: " + parent.N);
		System.out.println("g: " + parent.g);
		System.out.println("e: " + parent.e);
		System.out.println("v: " + new BigInteger(1, parent.v));
		System.out.println("cs: " + parent.in.chunkSize);
		System.out.println("fc: " + parent.in.fileChunks);

		System.out.println("T: " + T);
		System.out.println("M: " + M);
		System.out.println("coefficientKey: " + new BigInteger(1, coefficientKey));
		System.out.println("chunkKey: " + new BigInteger(1, chunkKey));

		// tau = T^e
		BigInteger tau = T.modPow(parent.e, parent.N);
		System.out.println("tau: " + tau);

		// tau = tau*(h(W_i)^(a_i)) for each i in chunkSet.
		for (int i = 0; i < chunkSet.size(); i++) {
			int chunkIndex = chunkSet.get(i);
			System.out.println("Verifying proof on chunk " + chunkIndex + " (" + (i + 1) + " out of " + chunkSet.size()
					+ ").");
			BigInteger hW_i = parent.gethW_i(chunkIndex);
			System.out.println("W_i: " + hW_i);
			BigInteger a_i = getCoefficient(chunkIndex);
			System.out.println("a_i: " + a_i);
			tau = tau.multiply(hW_i.modPow(a_i, parent.N)).mod(parent.N);
			System.out.println("tau: " + tau);
		}

		// We check g^M == tau
		BigInteger gM = parent.g.modPow(M, parent.N);
		System.out.println("gM: " + gM);

		assert (BigInteger.valueOf(M.bitLength()).compareTo(RSA_POR_gen.lambda) < 0);
		return gM.equals(tau);
	}
}
