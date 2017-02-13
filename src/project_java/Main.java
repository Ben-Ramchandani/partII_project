package project_java;

import java.math.BigInteger;
import java.nio.file.Paths;

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

	public static void printUsage() {
		System.err.println("Usage: merkle [Options] [File]");
		System.err.println("-p, --proof-block <block_num>: Generate a proof for this block.");
		System.err.println("-h, --hex: Print the resulting hash as a hexadecimal value.");
		System.err.println("-c, --contract: Generate a contract for this file.");
		System.err.println("-s, --script: Generate a deployment script for this file.");
		System.err
				.println("-m, --multi: Generate proofs for multiple chunks of this file based on the blockhash provided.");
	}

	public static void main(String[] args) throws Throwable {
		String fileName;
		Options options = new Options();

		Option proofBlock = new Option("p", "proof-block", true, "Generate a proof for this block.");
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
				"Generate proofs for multiple chunks of this file based on the blockhash provided.");
		multiProof.setRequired(false);
		options.addOption(multiProof);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

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

		ChunkStream b = new ChunkStream(Paths.get(fileName), Main.blockSize);
		Merkle m = new Merkle(b);
		if (cmd.hasOption("m")) {
			if (cmd.hasOption("p")) {
				System.err.println("--multi and --proof options are mutually exclusive.");
			}
			byte[] blockHash = DatatypeConverter.parseHexBinary(cmd.getOptionValue("m"));
			assert(blockHash.length == 32);
			int numProofs = ContractGen.numProofChunks;
			byte[] proof = new byte[0];
			for(int i=0;i<numProofs;i++) {
				int proofChunk = (new BigInteger(1, blockHash)).mod(BigInteger.valueOf(b.fileBlocks)).intValueExact();
				System.err.println("Generating proof for chunk " + proofChunk + " (" + (i+1) + " out of " + numProofs + ").");
				proof = Util.byteCombine(proof, m.proof(proofChunk));
			}
						
			if (cmd.hasOption("c") || cmd.hasOption("s")) {
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
		} else if (cmd.hasOption("p")) {
			int proofBlockNum = Integer.parseInt(cmd.getOptionValue("p"));
			byte[] proof = m.proof(proofBlockNum);
			if (cmd.hasOption("c") || cmd.hasOption("s")) {
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
}
