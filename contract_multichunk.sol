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
            block.number <= validFromBlock
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

        bytes32 currentHash = block.blockhash(validFromBlock);

        for(uint i=0; i<NUM_PROOF_CHUNKS; i++) {
            uint currentProofChunk = uint(currentHash) % BLOCKS_IN_FILE;
            if(!validateProof(proof, currentProofChunk)) {
                return 2;
            }
            assembly {
                proof := add(proof, SINGLE_CHUNK_PROOF_LENGTH)
            }
            currentHash = sha3(currentHash);
        }
        return 0;
        /*suicide(msg.sender);*/
    }
    
    function recover() {
        if(msg.sender == owner && block.number > validFromBlock + 255) {
            suicide(msg.sender);
        }
    }
    
    function test(bytes32 arg) returns (bytes32 res) {
        return arg;
    }

    function getBlockHash() returns (bytes32 hash) {
        return block.blockhash(validFromBlock);
    }
    
    function proofBlocks() returns (uint[NUM_PROOF_CHUNKS] chunks) {
        uint[NUM_PROOF_CHUNKS] memory chunks;
        bytes32 currenthash = block.blockhash(validFromBlock);
        for(uint i = 0; i < NUM_PROOF_CHUNKS; i++) {
            chunks[i] = uint(currenthash) % BLOCKS_IN_FILE;
            currentHash = sha3(currentHash);
        }
        return chunks;
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
            proofOffset += 1;
        }
        return hash == ROOT_HASH;
    }
}