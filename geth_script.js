if (eth.accounts.length > 0) {
	var primary = eth.coinbase;
	console.log("Please unlock your account.");
	if (!personal.unlockAccount(primary)) { exit(3) }
} else {
	console.log("No account found.");
	exit(2);
}
var fpSource = 'SCRIPT_CODE';
var fpCompiled = web3.eth.compile.solidity(fpSource);
var fpContract = web3.eth.contract(fpCompiled.SCRIPT_NAME.info.abiDefinition);

var filepay = fpContract.new({ from: primary, data: fpCompiled.SCRIPT_NAME.code, gas: 1000000 }, function (e, contract) {
	if (!e) {
		if (!contract.address) {
			console.log("Contract transaction send: TransactionHash: " + contract.transactionHash + " waiting to be mined...");
		} else {
			console.log("Contract mined! Address: " + contract.address);
		}
	} else {
		console.log("An error occurred.");
		console.error(e);
	}
});

filepay.status = function () {
	console.log("Contract is at address " + filepay.address + ".");
	console.log("Current block is " + eth.blockNumber + ".");
	console.log("Contract is valid from " + filepay.getValidFrom.call() + ".");
	if (filepay.validToCall.call()) {
		blockHash = filepay.getBlockHash.call();
		console.log("Contract is ready to be paid, block hash is " + blockHash + ".");
		console.log("To generate a proof run:");
		console.log("filepay SCRIPT_FILE_NAME -p " + blockHash + " -c -m NUM_PROOF_CHUNKS SCRIPT_EXTRA_ARGS");
	} else {
		console.log("Contract reports not ready.");
	}
}

console.log("Running miner for one block...");
miner.start(); admin.sleepBlocks(1); miner.stop();
console.log("Complete");

var simpleCallback = function(e, res) {
	if(!e) {
		console.log("simpleCallback got result.");
		console.log(res);
	} else {
		console.error("simpleCallback got error.");
		console.error(e);
	}
}

//"SCRIPT_NAME.submitProof.sendTransaction"