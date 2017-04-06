package project_java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class ContractGenerator {
	public static final int blocksBeforeValid = -1;
	public static final int lockInByBlocks = 2;
	public static final double paymentMultiplyer = 1.0;

	@SuppressWarnings("unused")
	public static String generate(Merkle merkleTree, String contractSkeletonFile, int numProofChunks)
			throws IOException {

		int depth = merkleTree.depth;
		int blockSize = merkleTree.chunkSize;
		byte[] rootHash = merkleTree.rootHash();
		long fileChunks = merkleTree.fileChunks;
		int proofLength = merkleTree.proofLength / 32;

		ArrayList<Replacement> replacements = new ArrayList<Replacement>();

		// Can't add negative int constants to uints in Solidity, but taking away positive numbers is fine.
		replacements.add(new Replacement("PM_BLOCKS_BEFORE_VALID", (blocksBeforeValid > 0 ? "+" : "")
				+ Integer.toString(blocksBeforeValid)));
		replacements.add(new Replacement("ROOT_HASH", "0x" + DatatypeConverter.printHexBinary(rootHash)));
		replacements.add(new Replacement("BLOCKS_IN_FILE", Long.toString(fileChunks)));
		replacements.add(new Replacement("BLOCK_LENGTH_BYTES", Integer.toString(blockSize)));
		replacements.add(new Replacement("MERKLE_DEPTH", Integer.toString(depth)));
		replacements.add(new Replacement("SINGLE_CHUNK_PROOF_LENGTH_256_BITS", Integer.toString(proofLength)));
		replacements.add(new Replacement("PROOF_LENGTH_256_BITS", Integer.toString(proofLength * numProofChunks)));
		replacements.add(new Replacement("SINGLE_CHUNK_PROOF_LENGTH_BYTES", Integer.toString(proofLength * 32)));
		replacements.add(new Replacement("LOCK_IN_BY_BLOCKS", Integer.toString(lockInByBlocks)));
		replacements.add(new Replacement("PAYMENT_MULTIPLYER", Double.toString(paymentMultiplyer)));
		replacements.add(new Replacement("NUM_PROOF_CHUNKS", Integer.toString(numProofChunks)));

		return generate(replacements, contractSkeletonFile);
	}

	private static String generate(List<Replacement> replacements, String contractSkeletonFile) throws IOException {
		String contract = Util.readFile(contractSkeletonFile);

		for (Replacement replacement : replacements) {
			contract = replacement.replace(contract);
		}

		return contract;
	}

	public static String generate(RSA_POR rsa, String contractSkeletonFile) throws IOException {
		ArrayList<Replacement> replacements = new ArrayList<Replacement>();
		assert (rsa.len_N <= 256);

		replacements.add(new Replacement("BLOCKS_BEFORE_VALID", Integer.toString(blocksBeforeValid)));
		replacements.add(new Replacement("BLOCKS_IN_FILE", Long.toString(rsa.in.fileChunks)));
		// replacements.add(new Replacement("BLOCK_LENGTH_BYTES",
		// Integer.toString(rsa.in.chunkSize)));
		replacements.add(new Replacement("LOCK_IN_BY_BLOCKS", Integer.toString(lockInByBlocks)));
		replacements.add(new Replacement("PAYMENT_MULTIPLYER", Double.toString(paymentMultiplyer)));
		replacements.add(new Replacement("NUM_PROOF_CHUNKS", Integer.toString(rsa.numProofChunks)));

		replacements.add(new Replacement("RSA_CONST_E", rsa.e));
		replacements.add(new Replacement("RSA_CONST_V", rsa.v));
		replacements.add(new Replacement("RSA_CONST_G", rsa.g));
		replacements.add(new Replacement("RSA_CONST_N", rsa.N));

		return generate(replacements, contractSkeletonFile);
	}
}
