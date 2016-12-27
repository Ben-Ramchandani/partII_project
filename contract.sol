pragma solidity ^0.4.0;
contract FilePay {
    
    uint validFromBlock;
    address owner;
    
    function FilePay() {
        owner = msg.sender;
        validFromBlock = block.number + BLOCKS_BEFORE_VALID;
    }
    
    function submitProof(bytes32[PROOF_LENGTH_256_BITS] proof) {
        if(
            block.number < validFromBlock
            || block.number > validFromBlock + 255) {
                throw;
            }
        if(!validateProof(proof, proofBlock())) {
            throw;
        }
        suicide(msg.sender);
    }
    
    function recover() {
        if(msg.sender == owner && block.number > validFromBlock + 255) {
            suicide(msg.sender);
        }
    }
    
    function test() returns (bytes32 res) {
        bytes32[2] memory arr = [bytes32(0x3737373737373737373737373737373737373737373737373737373737373737), bytes32(0x3737373737373737373737373737373737373737373737373737373737373737)];
        return sha3(arr);
        //return 0x3737373737373737373737373737373737373737373737373737373737373737;
        //return sha3(0x3737373737373737373737373737373737373737373737373737373737373737);
    }
    
    function shaSlice(uint[1] arr, uint start, uint amount) returns (bytes32 res) {
        assembly {
            res:=sha3(add(arr, start), amount)
        }
    }
    
    function proofBlock() internal returns (uint i) {
        uint h = uint(block.blockhash(validFromBlock));
        return h % BLOCKS_IN_FILE;
    }
    
    function validateProof(bytes32[PROOF_LENGTH_256_BITS] memory proof, uint i)
        internal returns (bool valid) {
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