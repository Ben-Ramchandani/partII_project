package project_java;

import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.*;

public class Main {
	// Must be a multiple of 32.
	public static final int blockSize = 32;

	public static void printUsage() {
		System.err.println("Usage: merkle [Options] [File]");
		System.err.println("-p, --proof-block <block_num>: Generate a proof for this block.");
		System.err.println("-h, --hex: Print the resulting hash as a hexadecimal value.");
		System.err.println("-c, --contract: Generate a contract for this file.");
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
		Option genContract = new Option("c", "contract", true, "Generate a contract for this file.");
		genContract.setRequired(false);
		options.addOption(genContract);

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

		BlockStream b = new BlockStream(Paths.get(fileName), Main.blockSize);
		Merkle m = new Merkle(b);
		if (cmd.hasOption("p")) {
			int proofBlockNum = Integer.parseInt(cmd.getOptionValue("p"));
			if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(m.proof(proofBlockNum)));
			} else if (cmd.hasOption("c")) {
				byte[] proof = m.proof(proofBlockNum);
				System.out.print("[");
				System.out.print("0x" + DatatypeConverter.printHexBinary(Util.slice(proof, 0, 32)));
				for (int i = 32; i < proof.length; i += 32) {
					System.out.print(", 0x" + DatatypeConverter.printHexBinary(Util.slice(proof, i, i + 32)));
				}
				System.out.println("]");
				System.exit(0);
			} else {
				System.out.write(m.proof(proofBlockNum));
			}
		} else if (cmd.hasOption("c")) {
			String contractSkeletonFile = cmd.getOptionValue("c");
			String contract = ContractGen.generate(m, contractSkeletonFile);
			System.out.println(contract);
		} else {
			if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(m.rootHash()));
			} else {
				System.out.write(m.rootHash());
			}
		}
	}
}
