const wallet = require("../src/wallet.js");
const keccak256 = require('keccak256');
const Signature = require("elliptic/lib/elliptic/ec/signature.js");
var EC = require('elliptic').ec;
var ec = new EC('secp256k1');

async function Transaction(){

    await setTimeout(async() => { 
        await wallet.Status()
        await wallet.Verify("3131313131313131")
        var acc = await wallet.GetAccount("01")

        await wallet.SelectAccount("01")

        var rlp_encode = "eb0d849502f907830aae60942cec9fc6270b3911d4d290334f459f0a830b0c3a87038d7ea4c68000802a8080"
        await wallet.RLP_Decode(rlp_encode)
        await wallet.HashKeccak256("ef7e80213ae81ce1a634404d12e59be30145a26cc3f12fed469f50f9fa4971a0")
        var singature = await wallet.Transaction("3131313131313131")

        var key = ec.keyFromPublic("04e5e2c9fdab26505629bedbbfd86b74df003f767fe8303fd033cd44ac2b425ea1d95e9472f1b11f38663ca6fe33e260df41be89200118b46929b469794fd0cfc8", 'hex')
        var msgHash = await keccak256(Buffer.from(rlp_encode, 'hex'))
    

        let s = parseInt(singature.s, 16)
        let n = parseInt(ec.n, 16)
        console.log(n.toString(16));
        n = n /2
        console.log(n.toString(16));
        console.log(s.toString(16));

        if(n > s)
            console.log("N geater");
        else
            console.log("S geater");
        console.log(key.verify(msgHash, singature));   
        console.log(ec.getKeyRecoveryParam(msgHash, singature, key.getPublic()));
    }, 1000);
}

Transaction()