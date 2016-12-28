pragma solidity ^0.4.0;
contract FilePay {
    
    uint validFromBlock;
    address owner;
    
    function FilePay() {
        owner = msg.sender;
        validFromBlock = block.number + BLOCKS_BEFORE_VALID;
    }
    
    function validToCall() returns (bool) {
        if(
            block.number < validFromBlock
            || block.number > validFromBlock + 255) {
            return false;
        } else {
            return true;
        }
    }
    
    function getValidFrom() returns (uint) {
    	return validFromBlock;
    }
    
    function submitProof(bytes32[PROOF_LENGTH_256_BITS] proof) returns (uint) {
        if(!validToCall()) {
            return 1;
        }
        if(!validateProof(proof, proofBlock())) {
            return 2;
        }
        suicide(msg.sender);
    }
    
    function recover() {
        if(msg.sender == owner && block.number > validFromBlock + 255) {
            suicide(msg.sender);
        }
    }
    
    function test(bytes32 arg) returns (bytes32 res) {
        return arg;
    }
    
    function proofBlock() returns (uint i) {
        uint h = uint(block.blockhash(validFromBlock));
        return h % BLOCKS_IN_FILE;
    }
    
    function validateProof(bytes32[PROOF_LENGTH_256_BITS] memory proof, uint i)
        returns (bool valid) {
        bytes32 hash;
        uint proofOffset = BLOCK_LENGTH_BYTES / 32;
        bytes32[2] memory hashTarget;
        assembly {
            hash:=sha3(proof, BLOCK_LENGTH_BYTES)
        }
        for(uint n = 0; n < MERKLE_DEPTH; n++) {
            bytes32 otherHash = proof[proofOffset];
            if(i % 2 == 0) {
                hashTarget[0] = hash;
                hashTarget[1] = otherHash;
            } else {
                hashTarget[0] = otherHash;
                hashTarget[1] = hash;
            }
            hash = sha3(hashTarget);
            i /= 2;
            proofOffset += 32;
        }
        return hash == ROOT_HASH;
    }
}