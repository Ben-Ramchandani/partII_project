pragma solidity ^0.4.0;

contract RSA {
    
    uint N;
    uint v;
    uint numChunks;
    uint e;
    
    function validateRSAProof(uint k, uint M, uint T) returns (bool res) {
        uint tau = modpow(T, e, N);
        
        for(uint i = 0; i < numChunks; i++) {
            uint W_i = HMAC(i, v);
            uint a_i = HMAC(i, k);
        }
    }
    
    function test() returns (uint ret) {
        return 3 >> 1;
    }
    
    function modpow(uint a, uint e, uint N) returns (uint res) {
        res = 1;
        a = a % N;
        while(e > 0) {
            if(e % 2 == 1) {
                res = mulmod(res, a, N);
            }
            a = mulmod(a, a, N);
            e = e >> 1;
        }
        return res;
    }
    
    function test_modpow() returns (uint res) {
        return modpow(
            115792089237316195423570985008687907853269984665640564039457584007913129639926,
            10,
            115792089237316195423570985008687907853269984665640564039457584007913129639933
        );
    }
    
    // k has length 128 bits.
    // This could maybe be XOR?
    function HMAC(uint i, uint k) returns (uint res) {
        return uint(sha3(k | (i << 128)));
    }

}