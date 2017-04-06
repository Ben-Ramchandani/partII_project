package project_java;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;

public class RSA_CLI {

	public CommandLine cmd;
	public String fileName;
	public AChunkStream stream;
	public PrintStream out;

	public RSA_CLI(CommandLine cmd, AChunkStream stream, PrintStream outFile, String fileName) throws IOException {
		this.cmd = cmd;
		this.stream = stream;
		this.out = outFile;
		this.fileName = fileName;
	}

	private String keyFile() {
		if (cmd.hasOption("k")) {
			return cmd.getOptionValue("k");
		} else {
			return fileName + ".keys";
		}
	}

	private String privateKeyFile() {
		return keyFile() + ".private";
	}

	private String tagsFile() {
		if (cmd.hasOption("t")) {
			return cmd.getOptionValue("t");
		} else {
			return fileName + ".tags";
		}
	}

	private void tagFile(BigInteger privateKey) throws IOException {
		AChunkStream stream = new ChunkStream(Paths.get(fileName), Main.chunkSizeRSA);
		RSA_POR r = new RSA_POR(keyFile(), stream, 10);
		r.tagAll(new FileOutputStream(tagsFile()), privateKey);
	}

	public void generateAndTag() throws IOException {
		System.err.println("Generating new RSA keys. Writing to " + keyFile() + " and " + privateKeyFile() + ".");
		BigInteger privateKey = RSA_POR_gen.generate(new PrintStream(keyFile()),
				privateKeyFile() != null ? new PrintStream(privateKeyFile()) : null);
		System.err.println("Generating tags for " + fileName + ", writing to " + tagsFile() + ".");
		tagFile(privateKey);
	}

	public void generateProof() throws IOException {

		byte[] blockHash = Main.parseBlockHash(cmd.getOptionValue("p"));

		int numProofChunks = Main.numProofChunks(cmd);

		RSA_POR r = new RSA_POR(keyFile(), stream, numProofChunks);
		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, blockHash);

		AChunkStream tags = new ChunkStream(tagsFile(), r.len_N + 1);

		RSA_Proof proof = chal.genProof(tags);
		if (cmd.hasOption("c") || cmd.hasOption("s")) {
			out.println(proof.toString());
		} else {
			proof.printTo(out);
		}
	}

	public void verifyProof() throws IOException {

		byte[] blockHash = Main.parseBlockHash(cmd.getOptionValue("p"));

		int numProofChunks = Main.numProofChunks(cmd);

		RSA_POR r = new RSA_POR(keyFile(), stream, numProofChunks);
		RSA_POR_Challenge chal = new RSA_POR_Challenge(r, blockHash);
		RSA_Proof proof = new RSA_Proof(cmd.getOptionValue("v"));

		if (chal.checkProof(proof)) {
			out.println("Verified successfully");
		} else {
			out.println("Verify failed");
		}
	}

	public void generateContract() throws IOException {
		String contractSkeletonFile = Main.RSAContractSkeletonFile;
		RSA_POR r = new RSA_POR(keyFile(), stream, Main.numProofChunks(cmd));
		String contract = ContractGenerator.generate(r, contractSkeletonFile);
		if (cmd.hasOption("s")) {
			String scriptSkeletonFile = Main.scriptSkeletonFile;
			String script = ScriptGenerator.generate(scriptSkeletonFile, contract, Main.contractName, fileName,
					Main.numProofChunks(cmd), true, cmd.hasOption("z"), this.stream.fileSize);
			out.println(script);
		} else {
			out.println(contract);
		}
	}
}
