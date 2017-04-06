package project_java;

import java.io.IOException;
import java.util.ArrayList;

public class ScriptGenerator {

	public static String generate(String scriptSkeletonFile, String contract, String contractName, String fileName,
			int numProofChunks, boolean isRSA, boolean isRandom, int fileSize) throws IOException {

		ArrayList<Replacement> replacements = new ArrayList<Replacement>();

		// The name of the input file.
		replacements.add(new Replacement("SCRIPT_FILE_NAME", fileName));
		replacements.add(new Replacement("NUM_PROOF_CHUNKS", Integer.toString(numProofChunks)));
		// JS doesn't have multi-line strings, so we strip out all new lines
		// (have to be careful with line comments and EVM assembly).
		replacements.add(new Replacement("SCRIPT_CODE", contract.replaceAll("\n", "").replaceAll("    ", "")));
		replacements.add(new Replacement("SCRIPT_NAME", contractName));
		replacements
				.add(new Replacement("SCRIPT_EXTRA_ARGS", (isRSA ? "-r" : "") + (isRandom ? " -z " + fileSize : "")));
		String scriptSkeleton = Util.readFile(scriptSkeletonFile);

		for (Replacement replacement : replacements) {
			scriptSkeleton = replacement.replace(scriptSkeleton);
		}
		return scriptSkeleton;
	}

	public static String generate(String scriptSkeletonFile, String contract, String contractName, String fileName,
			int numProofChunks, boolean isRSA) throws IOException {
		return generate(scriptSkeletonFile, contract, contractName, fileName, numProofChunks, isRSA, false, 0);
	}
}