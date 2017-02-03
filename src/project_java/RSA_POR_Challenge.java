package project_java;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RSA_POR_Challenge {
	public RSA_POR parent;
	public byte[] chunkKey;
	public byte[] coefficientKey;
	public long c;

	public RSA_POR_Challenge(RSA_POR parent, byte[] chunkKey, byte[] aCoefficientkey, long numChunks) {
		this.parent = parent;
		this.chunkKey = chunkKey;
		this.coefficientKey = aCoefficientkey;
		this.c = numChunks;
	}

	public BigIntegerPair genProofSingle(byte[] coefficientKey, int chunkIndex, byte[] chunkBytes,
			byte[] chunkTagBytes) {
		BigInteger a = new BigInteger(Util.HMAC(coefficientKey, Util.toBytes(chunkIndex)));
		BigInteger chunk = new BigInteger(chunkBytes);
		BigInteger chunkTag = new BigInteger(chunkTagBytes);

		BigInteger T = chunkTag.modPow(a, parent.N);
		BigInteger M = chunk.multiply(a);
		return new BigIntegerPair(T, M);
	}

	public List<Integer> getChunkSet(long numChunks) {
		List<Integer> res = new ArrayList<Integer>();
		assert (numChunks <= parent.in.fileBlocks);
		for (int i = 0; i < numChunks; i++) {
			BigInteger chunkKeyInt = new BigInteger(chunkKey);
			res.add(chunkKeyInt.mod(BigInteger.valueOf(parent.in.fileBlocks)).intValue());
			chunkKey = Util.hash(chunkKey);
		}
		return res;
	}

	public List<BigInteger> getCoefficientSet(List<Integer> chunkSet) {
		List<BigInteger> res = new ArrayList<BigInteger>();
		Iterator<Integer> it = chunkSet.iterator();
		while (it.hasNext()) {
			int chunkIndex = it.next();
			byte[] a = Util.HMAC(coefficientKey, Util.toBytes(chunkIndex));
			res.add(new BigInteger(a));
		}
		return res;
	}

	public BigIntegerPair genProof(long numChunks, byte[][] chunkTagBytes) {
		List<Integer> chunkSet = getChunkSet(numChunks);
		List<BigInteger> coefficientSet = getCoefficientSet(chunkSet);

		BigInteger T = BigInteger.ONE;
		BigInteger M = BigInteger.ZERO;
		
		for (int i = 0; i < chunkSet.size(); i++) {
			int chunkIndex = chunkSet.get(i);
			BigInteger chunk = new BigInteger(parent.in.getChunk(chunkIndex));
			BigInteger a = coefficientSet.get(chunkIndex);
			
			T = T.multiply(chunk.modPow(a, parent.N));
			
			M = M.add(chunk.multiply(a));
		}
		
		return new BigIntegerPair(T, M);
	}
	
	public boolean checkProof(BigIntegerPair proof) {
		
		return false;
	}
}
