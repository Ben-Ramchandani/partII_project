package project_java;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	// Must be a multiple of 32.
	public static final int chunkSize = 64;
	public static final int chunkSizeRSA = 16;
	public static final String merkleContractSkeletonFile = "contract_multichunk.sol";
	public static final String RSAContractSkeletonFile = "contract_RSA.sol";
	public static final String scriptSkeletonFile = "geth_script.js";
	public static final String contractName = "FilePay";

	public static void printUsage() {
		System.err.println("Usage: merkle [Options] [File]");
		System.err
				.println("-p, --proof <block_num | block_hash>: An integer representing a chunk index or a hez string representing a block hash,"
						+ "generate a proof for this chunk, or for chunks based on the block hash provided.");
		System.err.println("-h, --hex: Print the resulting hash as a hexadecimal value.");
		System.err.println("-c, --contract: Generate a contract for this file.");
		System.err.println("-s, --script: Generate a deployment script for this file.");
		System.err
				.println("-m, --multi: Generate proofs for the number of chunks provided. --proof is required and must be a blockhash.");
		System.err.println("-r, --rsa <file>: Use the given RSA keyfile.");
	}

	public static void main(String[] args) throws Throwable {
		String fileName;
		Options options = new Options();

		/*
		 * Declare options.
		 */
		Option proofBlock = new Option("p", "proof", true,
				"An integer representing a chunk index or a hez string representing a block hash,"
						+ "generate a proof for this chunk, or for chunks based on the block hash provided.");
		options.addOption(proofBlock);

		Option printHex = new Option("h", "hex", false, "Print the resulting hash as a hexadecimal value.");
		options.addOption(printHex);

		Option genContract = new Option("c", "contract", false, "Generate a contract for this file.");
		options.addOption(genContract);

		Option genScript = new Option("s", "script", false, "Generate a deployment script for this file.");
		options.addOption(genScript);

		Option multiProof = new Option("m", "multi", true,
				"Generate proofs for the number of chunks provided. --proof is required and must be a blockhash.");
		options.addOption(multiProof);

		Option useRSA = new Option("r", "rsa", false, "Use the RSA POR system.");
		options.addOption(useRSA);

		Option tagRSA = new Option("t", "tags-rsa", true, "Use this RSA tag file.");
		options.addOption(tagRSA);

		Option keyRSA = new Option("k", "keys-rsa", true, "Use this RSA key file.");
		options.addOption(keyRSA);

		Option out = new Option("o", "out", true, "File to print to (defaults to stdout).");
		options.addOption(out);

		Option verify = new Option("v", "verify", true, "Verify an existing proof.");
		options.addOption(verify);

		Option zero = new Option("z", "zero", true, "Read from /dev/zero with the specified file size.");
		options.addOption(zero);

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
			} else if (!cmd.hasOption("z")) {
				System.err.println("No file specified.");
				HelpFormatter h = new HelpFormatter();
				h.printHelp("filepay <options> <file>", options);
				System.exit(1);
				return;
			} else {
				fileName = "dev/random";
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter h = new HelpFormatter();
			h.printHelp("filepay <options> <file>", options);
			System.exit(1);
			return;
		}

		/*
		 * Initialise
		 */
		PrintStream outFile = System.out;
		if (cmd.hasOption("o")) {
			outFile = new PrintStream(cmd.getOptionValue("o"));
		}

		if (cmd.hasOption("r")) {
			/*
			 * RSA
			 */
			AChunkStream b;
			if (cmd.hasOption("z")) {
				b = new RandomStream(Integer.parseInt(cmd.getOptionValue("z")), Main.chunkSizeRSA);
			} else {
				b = new ChunkStream(fileName, Main.chunkSizeRSA);
			}
			RSA_CLI cli = new RSA_CLI(cmd, b, outFile);
			if (cmd.hasOption("v")) {
				assert (cmd.hasOption("p")) : "Verify requires a block hash be specified with -p.";
				cli.verifyProof();
			} else if (cmd.hasOption("p")) {
				cli.generateProof();
			} else if (cmd.hasOption("c") || cmd.hasOption("s")) {
				cli.generateContract();
			} else {
				cli.generateAndTag();
			}
		} else {
			/*
			 * Merkle
			 */
			AChunkStream b;

			if (cmd.hasOption("z")) {
				b = new RandomStream(Integer.parseInt(cmd.getOptionValue("z")), Main.chunkSize);
			} else {
				b = new ChunkStream(fileName, Main.chunkSize);
			}
			Merkle_CLI cli = new Merkle_CLI(cmd, b, outFile);
			if (cmd.hasOption("p")) {
				cli.generateProof();
			} else if (cmd.hasOption("c") || cmd.hasOption("s")) {
				cli.generateContract();
			} else {
				cli.printRootHash();
			}
		}
	}

	public static byte[] parseBlockHash(String in) {
		if (in.length() == 66) {
			in = in.substring(2);
		}
		byte[] blockHash = DatatypeConverter.parseHexBinary(in);
		assert (blockHash.length == 32);
		return blockHash;
	}

	public static List<Integer> getProofChunks(CommandLine cmd, int fileChunks) {
		assert (cmd.hasOption("p"));
		int numProofChunks = Main.numProofChunks(cmd);
		String proofArg = cmd.getOptionValue("p");

		if (numProofChunks > 1 || proofArg.length() == 64 || proofArg.length() == 66) {
			byte[] blockHash = Main.parseBlockHash(proofArg);
			return Main.getProofChunks(blockHash, numProofChunks, fileChunks);
		} else {
			int proofChunkNum = Integer.parseInt(cmd.getOptionValue("p"));
			assert (proofChunkNum >= 0 && proofChunkNum < fileChunks);
			List<Integer> proofChunks = new ArrayList<Integer>();
			proofChunks.add(proofChunkNum);
			return proofChunks;
		}
	}

	public static List<Integer> getProofChunks(byte[] blockHash, int numChunks, int fileChunks) {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < numChunks; i++) {
			res.add((new BigInteger(1, blockHash)).mod(BigInteger.valueOf(fileChunks)).intValueExact());
			blockHash = Util.hash(blockHash);
		}
		return res;
	}

	public static int numProofChunks(CommandLine cmd) {
		int res = 1;
		if (cmd.hasOption("m")) {
			res = Integer.parseInt(cmd.getOptionValue("m"));
			assert (res > 0 && res <= 128);
		}
		return res;
	}
}
