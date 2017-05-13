# Ben Ramchandani - bjr39 - Part II project 2017 - source code


This source code submission includes:
* Various versions of the contract (in `contracts/`) including the base version and various extensions.
* The Java source code (in `src/`) for the proof generation. The libraries RNRT SAPHIR (saphir-hash) and Apache Commons CLI (commons-cli-1.3.1) are required to compile it, they are not included in this submission.
* A working executable .jar file (`filepay.jar`) including the above libraries.
* The git repository (`.git`) which contains the project's history.

Example useage is `java -jar filepay.jar demo/demo.file -s -o demo/demo.js`, which produces a script `demo/demo.js` which can then be loaded using the go-ethereum command line client `geth` via `loadScript()`.