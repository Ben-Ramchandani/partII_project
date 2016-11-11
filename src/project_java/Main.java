package project_java;

import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.*;

public class Main {
	public static final int blockSize = 1024;

	public static void printUsage() {
		System.err.println("Usage: merkle [Options] [File]");
		System.err.println("-p, --proof-block <block_num>: Generate a proof for this block.");
		System.err.println("-h, --hex: Print the resulting hash as a hexadecimal value.");
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

		if (cmd.hasOption("p")) {
			int proofBlockNum = Integer.parseInt(cmd.getOptionValue("p"));
			BlockStream b = new BlockStream(Paths.get(fileName), Main.blockSize);
			Merkle m = new Merkle(b);
			if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(m.proof(proofBlockNum)));
			} else {
				System.out.write(m.proof(proofBlockNum));
			}
		} else {
			BlockStream b = new BlockStream(Paths.get(fileName), Main.blockSize);
			Merkle m = new Merkle(b);
			if (cmd.hasOption("h")) {
				System.out.println(DatatypeConverter.printHexBinary(m.rootHash()));
			} else {
				System.out.write(m.rootHash());
			}
		}
	}
}
