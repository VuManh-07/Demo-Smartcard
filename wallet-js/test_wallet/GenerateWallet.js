const wallet = require("../src/wallet");

async function test(){ 
    await wallet.GetStatus()
    console.log(await wallet.GenerateHDWallet());
}

setTimeout(()=>{
    test()

}, 2000)