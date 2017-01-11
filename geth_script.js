if(eth.accounts.length > 0) {
	var primary = eth.coinbase;
	console.log("Please unlock your account.");
	if(!personal.unlockAccount(primary)) {exit(3)}
} else {
	console.log("No account found.");
	exit(2);
}
var fpSource = '<CODE>';
var fpCompiled = web3.eth.compile.solidity(fpSource);
var fpContract = web3.eth.contract(fpCompiled.<NAME>.info.abiDefinition);

var filepay = fpContract.new({from: primary, data: fpCompiled.<NAME>.code, gas: 1000000}, function(e, contract) {
	if(!e) {
		if(!contract.address) {
			console.log("Contract transaction send: TransactionHash: " + contract.transactionHash + " waiting to be mined...");
		} else {
			console.log("Contract mined! Address: " + contract.address);
		}
	} else {
		console.log("An error occurred.");
		console.error(e);
	}
});
