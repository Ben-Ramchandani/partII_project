pragma solidity ^0.4.0;

contract FilePay {
    
    uint validFromBlock;
    address owner;
    
    function FilePay() {
        owner = msg.sender;
        validFromBlock = block.number + BLOCKS_BEFORE_VALID;
    }
    
    function validToCall() returns (bool) {
        return block.number > validFromBlock &&
            block.number <= validFromBlock + 255;
    }
    
    function getValidFrom() returns (uint) {
    	return validFromBlock;
    }
    
    function submitProof(uint T, uint M) returns (uint) {
        if(!validToCall()) {
            return 1;
        }
        if(!validateRSAProof(getBlockHash(), T, M)) {
            return 2;
        }
        suicide(msg.sender);
    }
    
    function recover() {
        if(msg.sender == owner && block.number > validFromBlock + 255) {
            suicide(msg.sender);
        }
    }

    function getBlockHash() returns (bytes32 hash) {
        return block.blockhash(validFromBlock);
    }
    
    function validateRSAProof(bytes32 k, uint T, uint M) returns (bool res) {
        bytes32 chunkKey = k;
        bytes32 coefficientKey = sha3(k);
        uint tau = powmod(T, RSA_CONST_E, RSA_CONST_N);
        
        for(uint j = 0; j < NUM_PROOF_CHUNKS; j++) {
            uint i = uint(chunkKey) % BLOCKS_IN_FILE;
            chunkKey = sha3(chunkKey);
            uint W_i = HMAC(i, RSA_CONST_V);
            uint a_i = HMAC(i, coefficientKey) & 0xffffffffffffffff;
            tau = mulmod(tau, powmod(W_i, a_i, RSA_CONST_N), RSA_CONST_N);
        }
        uint gm = powmod(RSA_CONST_G, M, RSA_CONST_N);
        return gm == tau;
    }
    
    function powmod(uint a, uint e, uint N) internal returns (uint res) {
        assembly {res:= 1}
        assembly {a:= mod(a, N)}
        while(e > 0) {
            if(e & 1 != 0) {
                res = mulmod(res, a, N);
            }
            assembly {a:= mulmod(a, a, N)}
            assembly {e:= div(e, 2)}
        }
        return res;
    }
    
    function HMAC(uint i, bytes32 k) returns (uint res) {
        return uint(sha3(uint(k) ^ i));
    }

}