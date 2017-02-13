pragma solidity ^0.4.0;
contract FilePayLockIn {
    
    uint validAfterBlock;
    uint lockInByBlock;
    uint interestValue;
    address owner;
    address lockedInAddress = 0;
    
    event LockedIn(address by);

    function FilePayLockIn() payable {
        owner = msg.sender;
        interestValue = msg.value;
        lockInByBlock = block.number + LOCK_IN_BY_BLOCKS;
        validAfterBlock = block.number + BLOCKS_BEFORE_VALID;
    }

    function lockIn() payable {
        if(msg.value >= requiredLockIn() && lockedInAddress == 0 && block.number < lockInByBlock) {
            lockedInAddress = msg.sender;
            LockedIn(msg.sender);
        } else {
            throw;
        }
    }

    function requiredLockIn() returns (uint) {
        return interestValue * PAYMENT_MULTIPLYER;
    }

    function lockInAddress() returns (address) {
        return lockedInAddress;
    }
    
    function validToCall() returns (bool) {
        if(
            block.number <= validAfterBlock
            || block.number > validAfterBlock + 255
            || msg.sender != lockedInAddress) {
            return false;
        } else {
            return true;
        }
    }
    
    function getValidFrom() returns (uint) {
    	return validAfterBlock;
    }

    function getLockInBy() returns (uint) {
        return lockInByBlock;
    }
    
    function submitProof(bytes32[PROOF_LENGTH_256_BITS] proof) returns (uint) {
        if(!validToCall()) {
            return 1;
        }
        if(!validateProof(proof, proofBlock())) {
            return 2;
        }
        return 0;
        /*suicide(msg.sender);*/
    }
    
    function recover() {
        if(msg.sender == owner && (block.number > validAfterBlock + 255 || (block.number >= lockInByBlock && lockedInAddress == 0))) {
            suicide(msg.sender);
        }
    }
    
    function proofBlock() returns (uint i) {
        uint h = uint(block.blockhash(validAfterBlock));
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
            proofOffset += 1;
        }
        return hash == ROOT_HASH;
    }
}
