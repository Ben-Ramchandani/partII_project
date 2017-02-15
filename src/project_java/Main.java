package project_java;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	// Must be a multiple of 32.
	public static final int blockSize = 32;
	public static final String contractSkeletonFile = "contract_lockin.sol";
	public static final String scriptSkeletonFile = "geth_script.js";
	public static final String contractName = "FilePayLockIn";
	public static int numProofChunks;

	public static void printUsage() {
		System.err.println("Usage: merkle [Options] [File]");
		System.err.println("-p, --proof <block_num | block_hash>: An integer representing a chunk index or a hez string representing a block hash,"
				+ "generate a proof for this chunk, or for chunks based on the block hash provided.");
		System.err.println("-h, --hex: Print the resulting hash as a hexadecimal value.");
		System.err.println("-c, --contract: Generate a contract for this file.");
		System.err.println("-s, --script: Generate a deployment script for this file.");
		System.err
				.println("-m, --multi: Generate proofs for the number of chunks provided. --proof is required and must be a blockhash.");
	}

	public static void main(String[] args) throws Throwable {
		String fileName;
		Options options = new Options();

		/*
		 * Declare options.
		 */
		Option proofBlock = new Option("p", "proof", true, "An integer representing a chunk index or a hez string representing a block hash,"
				+ "generate a proof for this chunk, or for chunks based on the block hash provided.");
		proofBlock.setRequired(false);
		options.addOption(proofBlock);
		Option printHex = new Option("h", "hex", false, "Print the resulting hash as a hexadecimal value.");
		printHex.setRequired(false);
		options.addOption(printHex);
		Option genContract = new Option("c", "contract", false, "Generate a contract for this file.");
		genContract.setRequired(false);
		options.addOption(genContract);
		Option genScript = new Option("s", "script", false, "Generate a deployment script for this file.");
		genScript.setRequired(false);
		options.addOption(genScript);
		Option multiProof = new Option("m", "multi", true,
				"Generate proofs for the number of chunks provided. --proof is required and must be a blockhash.");
		multiProof.setRequired(false);
		options.addOption(multiProof);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

		/*
		 * Parse options.
		 */
		try {
			cmd = parser.parse(options, args);
			String[] leftOver = cmd.getArgs();
			if (leftOver.length > 0) {
				fileName = leftOver[0];
			} else {
				System.err.println("No file specified.");
				Main.printUsage();
				System.exit(1);
				return;
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			Main.printUsage();
			System.exit(1);
			return;
		}

		/*
		 * Initialise Merkle tree.
		 */
		AChunkStream b = new ChunkStream(Paths.get(fileName), Main.blockSize);
		Merkle m = new Merkle(b);
		System.err.println(m.toString());
		
		/*
		 * Select behaviour.
		 */
		if(cmd.hasOption("m")) {
			Main.numProofChunks = Integer.parseInt(cmd.getOptionValue("m"));
			assert(Main.numProofChunks > 0 && Main.numProofChunks < 256);
		} else {
			Main.numProofChunks = 1;
		}
		
		if (cmd.hasOption("p")) {
			/*
			 * Generate a proof.
			 */
			String optionValue = cmd.getOptionValue("p");
			List<Integer> proofChunks;
			if (cmd.hasOption("m") || optionValue.length() == 64) {
				// Support for more than one chunk.
				byte[] blockHash = DatatypeConverter.parseHexBinary(optionValue);
				assert(blockHash.length == 32);
				System.err.println("Creating proofs for " + Main.numProofChunks + " blocks using block hash " + optionValue + ".");
				proofChunks = Main.getProofChunks(blockHash, Main.numProofChunks, b.fileChunks);
			} else {
				numProofChunks = 1;
				int proofChunkNum = Integer.parseInt(cmd.getOptionValue("p"));
				assert(proofChunkNum >= 0 && proofChunkNum < b.fileChunks);
				proofChunks = new ArrayList<Integer>();
				proofChunks.add(proofChunkNum);
			}
			
			byte[] proof = new byte[0];
			for(int i=0;i<proofChunks.size();i++) {
				int proofChunk = proofChunks.get(i);
				System.err.println("Generating proof for chunk " + proofChunk + " (" + (i+1) + " out of " + Main.numProofChunks + ").");
				proof = Util.byteCombine(proof, m.proof(proofChunk));
			}
			
			if (cmd.hasOption("c") || cmd.hasOption("s")) {
				//Print out the proof in a way that can be parsed in Web3/Geth for consumption by Ethereum.
				System.out.print("[");
				System.out.print("\"0x" + DatatypeConverter.printHexBinary(Util.slice(proof, 0, 32)) + "\"");
				for (int i = 32; i < proof.length; i += 32) {
					System.out.print(", \"0x" + DatatypeConverter.printHexBinary(Util.slice(proof, i, i + 32)) + "\"");
				}
				System.out.println("]");
				System.exit(0);
			} else if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(proof));
			} else {
				System.out.write(proof);
			}
			
		} else if (cmd.hasOption("c") || cmd.hasOption("s")) {
			/*
			 * Generate a contract (and possibly a contract deployment script for Geth).
			 */			
			String contractSkeletonFile = Main.contractSkeletonFile;
			String contract = ContractGen.generate(m, contractSkeletonFile);
			if (cmd.hasOption("s")) {
				String scriptSkeletonFile = Main.scriptSkeletonFile;
				String script = ScriptGen.generate(scriptSkeletonFile, contract, Main.contractName);
				System.out.println(script);
			} else {
				System.out.println(contract);
			}
		} else {
			if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(m.rootHash()));
			} else {
				System.out.write(m.rootHash());
			}
		}
	}
	
	private static List<Integer> getProofChunks(byte[] blockHash, int numChunks, int fileChunks) {
		List<Integer> res = new ArrayList<Integer>();
		BigInteger blockHashInt = new BigInteger(blockHash);
		for (int i = 0; i < numChunks; i++) {
			res.add(blockHashInt.mod(BigInteger.valueOf(fileChunks)).intValueExact());
			blockHashInt = Util.hash(blockHashInt);
		}
		return res;
	}
}
