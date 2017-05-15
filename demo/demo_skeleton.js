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
console.log("Mining 1 block");
miner.start(); admin.sleepBlocks(1); miner.stop();

if(web3.fromWei(eth.getBalance(secondary), "ether") != 1.0) {
    console.error("Balance is not 1 ether.");
    exit;
}

console.log("Compiling contract")
var fpSource = 'SCRIPT_CODE';
var fpCompiled = web3.eth.compile.solidity(fpSource);
var fpContract = web3.eth.contract(fpCompiled.SCRIPT_NAME.info.abiDefinition);

var filepay = fpContract.new({ from: coinbase, data: fpCompiled.SCRIPT_NAME.code, gas: 4000000, value: web3.toWei(1.0, "ether")}, function (e, contract) {
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
		console.log("java -jar filepay.jar SCRIPT_FILE_NAME -p " + blockHash + " -c -m NUM_PROOF_CHUNKS SCRIPT_EXTRA_ARGS");
	} else {
		console.log("Contract reports not ready.");
	}
}

console.log("Mining 1 block");
miner.start(); admin.sleepBlocks(1); miner.stop();

function getBalance(account) {
	return web3.fromWei(eth.getBalance(account), "ether") + " Ether"
}

function mine1() {
	miner.start(); admin.sleepBlocks(1); miner.stop();
}