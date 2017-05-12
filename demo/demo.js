/*
 * Setup script for a simple demo with geth.
 */

console.log("Creating accounts")
if(eth.accounts.length < 1) {
    personal.newAccount("");
	// Make sure the private chain has been generated.
	console.log("Mining 1 block");
	miner.start(); admin.sleepBlocks(1); miner.stop();
}
var coinbase = eth.coinbase;
var secondary = personal.newAccount("");
personal.unlockAccount(coinbase, "");
personal.unlockAccount(secondary, "");

console.log("Sending 1 ether to secondary");
eth.sendTransaction({from: coinbase, to: secondary, value: web3.toWei(1.0, "ether")});
miner.start(); admin.sleepBlocks(1); miner.stop();

if(web3.fromWei(eth.getBalance(secondary), "ether") != 1.0) {
    console.error("Balance is not 1 ether.");
    exit;
}

console.log("Compiling contract")
var fpSource = 'pragma solidity ^0.4.0;contract FilePay {uint validFromBlock;address owner;function FilePay() payable {owner = msg.sender;validFromBlock = block.number -1;}function validToCall() returns (bool) {if(block.number <= validFromBlock|| block.number > validFromBlock + 255) {return false;} else {return true;}}function getValidFrom() returns (uint) {	return validFromBlock;}function submitProof(bytes32[12] proof) returns (uint) {if(!validToCall()) {return 1;}bytes32 currentHash = block.blockhash(validFromBlock);for(uint i=0; i<1; i++) {uint currentProofChunk = uint(currentHash) % 951;if(!validateProof(proof, currentProofChunk)) {return 2;}assembly {proof := add(proof, 384)}currentHash = sha3(currentHash);}/*return 0;*/suicide(msg.sender);}function recover() {if(msg.sender == owner && block.number > validFromBlock + 255) {suicide(msg.sender);}}function test(bytes32 arg) returns (bytes32 res) {return arg;}function getBlockHash() returns (bytes32 hash) {return block.blockhash(validFromBlock);}function proofBlocks() returns (uint[1] memory chunks) {bytes32 currentHash = block.blockhash(validFromBlock);for(uint i = 0; i < 1; i++) {chunks[i] = uint(currentHash) % 951;currentHash = sha3(currentHash);}return chunks;}function validateProof(bytes32[12] memory proof, uint i)returns (bool valid) {bytes32 hash;uint proofOffset = 64 / 32;bytes32[2] memory hashTarget;assembly {hash:=sha3(proof, 64)}for(uint n = 0; n < 10; n++) {bytes32 otherHash = proof[proofOffset];if(i % 2 == 0) {hashTarget[0] = hash;hashTarget[1] = otherHash;} else {hashTarget[0] = otherHash;hashTarget[1] = hash;}hash = sha3(hashTarget);i /= 2;proofOffset += 1;}return hash == 0x265E953765895B2E4A567520AD9F96A117470CAFC4C17C3D5891E2CC704CD1D9;}}';
var fpCompiled = web3.eth.compile.solidity(fpSource);
var fpContract = web3.eth.contract(fpCompiled.FilePay.info.abiDefinition);

var filepay = fpContract.new({ from: coinbase, data: fpCompiled.FilePay.code, gas: 4000000, value: web3.toWei(1.0, "ether")}, function (e, contract) {
	if (!e) {
		if (!contract.address) {
			console.log("Contract transaction send: TransactionHash: " + contract.transactionHash + " waiting to be mined...");
		} else {
			console.log("Contract mined!");
			filepay.status()
		}
	} else {
		console.log("An error occurred.");
		console.error(e);
	}
});

filepay.status = function () {
	console.log("Contract is at address " + filepay.address + ".");
	console.log("Current block is " + eth.blockNumber + ".");
	console.log("Contract is valid after block " + filepay.getValidFrom.call() + ".");
	if (filepay.validToCall.call()) {
		blockHash = filepay.getBlockHash.call();
		console.log("Contract is ready to be paid, block hash is " + blockHash + ".");
		console.log("To generate a proof run:");
		console.log("java -jar filepay.jar demo/demo.file -p " + blockHash + " -c -m 1 ");
	} else {
		console.log("Contract reports not ready.");
	}
}

console.log("Mining 1 block");
miner.start(); admin.sleepBlocks(1); miner.stop();

function getBalance(account) {
	return web3.fromWei(eth.getBalance(account), "ether") + " Ether"
}
