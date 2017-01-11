package project_java;

import java.io.IOException;

public class ScriptGen {

	public static String generate(String scriptSkeletonFile, String contract, String contractName) throws IOException {
		
		Replacement codeReplace = new Replacement("<CODE>", contract.replaceAll("\n", "").replaceAll("    ", ""));
		Replacement nameReplace = new Replacement("<NAME>", contractName);
		String scriptSkeleton = Util.readFile(scriptSkeletonFile);
		return nameReplace.replace(codeReplace.replace(scriptSkeleton));
	}
}
