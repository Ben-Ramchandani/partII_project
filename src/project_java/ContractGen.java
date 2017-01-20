package project_java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

public class ContractGen {
	public static final int blocksBeforeValid = 4;
	public static final int lockInByBlocks = 2;
	public static final double paymentMultiplyer = 1.0; 
	
	public static String generate(Merkle merkleTree, String contractSkeletonFile) throws IOException {
		
		int depth = merkleTree.depth;
		int blockSize = merkleTree.blockSize;
		byte[] rootHash = merkleTree.rootHash();
		long fileBlocks = merkleTree.fileBlocks;
		int proofLength = merkleTree.proofLength / 32;
		
		ArrayList<Replacement> replacements = new ArrayList<Replacement>();
		
		replacements.add(new Replacement("BLOCKS_BEFORE_VALID", Integer.toString(blocksBeforeValid)));
		replacements.add(new Replacement("ROOT_HASH", "0x" + DatatypeConverter.printHexBinary(rootHash)));
		replacements.add(new Replacement("BLOCKS_IN_FILE", Long.toString(fileBlocks)));
		replacements.add(new Replacement("BLOCK_LENGTH_BYTES", Integer.toString(blockSize)));
		replacements.add(new Replacement("MERKLE_DEPTH", Integer.toString(depth)));
		replacements.add(new Replacement("PROOF_LENGTH_256_BITS", Integer.toString(proofLength)));
		replacements.add(new Replacement("LOCK_IN_BY_BLOCKS", Integer.toString(lockInByBlocks)));
		replacements.add(new Replacement("PAYMENT_MULTIPLYER", Double.toString(paymentMultiplyer)));

		
		Iterator<Replacement> it = replacements.iterator();
		
		String contract = Util.readFile(contractSkeletonFile);
		
		while(it.hasNext()) {
			Replacement replacement = it.next();
			contract = replacement.replace(contract);
		}
		
		return contract;
	}
}
