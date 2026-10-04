var EC = require('elliptic').ec;
var ec = new EC('secp256k1');

// Generate keys
var key = ec.genKeyPair();

var pri = key.getPrivate("hex")
var pub = key.getPublic("hex");

console.log("Private Key:", pri);
console.log("Public Key:", pub);

