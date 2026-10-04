const wallet = require("../src/wallet.js");

async function test(){ 
    await wallet.GetStatus();
    console.log("Reset Wallet:", await wallet.ResetWallet("d955927f877fbd0ba5c815d1bd006c272539d4bbc5d5a70ff58bef5567b9fb35f3171356bf22017e20ea9bb91050aed6eb06a0bdc004310c152f10e5334fd59a"));
    await wallet.GetStatus();
}

setTimeout(()=>{
    test()

}, 2000)