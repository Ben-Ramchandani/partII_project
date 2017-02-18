package project_java;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;

public class Merkle_CLI {

	public CommandLine cmd;
	public AChunkStream stream;
	public Merkle merkle;
	public PrintStream out;

	public Merkle_CLI(CommandLine cmd, AChunkStream stream, PrintStream outFile) throws IOException {
		this.cmd = cmd;
		this.stream = stream;
		this.merkle = new Merkle(stream);
		this.out = outFile;
	}

	public void printRootHash() throws IOException {
		if (cmd.hasOption("h")) {
			out.println(DatatypeConverter.printHexBinary(merkle.rootHash()));
		} else {
			out.write(merkle.rootHash());
		}
	}

	public void generateContract() throws IOException {
		String contractSkeletonFile = Main.merkleContractSkeletonFile;
		String contract = ContractGenerator.generate(merkle, contractSkeletonFile, Main.numProofChunks(cmd));
		if (cmd.hasOption("s")) {
			String scriptSkeletonFile = Main.scriptSkeletonFile;
			String script = ScriptGen.generate(scriptSkeletonFile, contract, Main.contractName);
			out.println(script);
		} else {
			out.println(contract);
		}
	}

	public void generateProof() throws IOException {
		/*
		 * Generate a proof.
		 */
		List<Integer> proofChunks = Main.getProofChunks(cmd, stream.fileChunks);

		byte[] proof = new byte[0];
		for (int i = 0; i < proofChunks.size(); i++) {
			int proofChunk = proofChunks.get(i);
			System.err.println("Generating proof for chunk " + proofChunk + " (" + (i + 1) + " out of "
					+ proofChunks.size() + ").");
			proof = Util.byteCombine(proof, merkle.proof(proofChunk));
		}

		if (cmd.hasOption("c") || cmd.hasOption("s")) {
			// Print out the proof in a way that can be parsed in Web3/Geth for
			// consumption by Ethereum.
			out.print("[");
			out.print("\"0x" + DatatypeConverter.printHexBinary(Util.slice(proof, 0, 32)) + "\"");
			for (int i = 32; i < proof.length; i += 32) {
				out.print(", \"0x" + DatatypeConverter.printHexBinary(Util.slice(proof, i, i + 32)) + "\"");
			}
			out.println("]");
			System.exit(0);
		} else if (cmd.hasOption("h")) {
			out.println(DatatypeConverter.printHexBinary(proof));
		} else {
			out.write(proof);
		}
	}
}
