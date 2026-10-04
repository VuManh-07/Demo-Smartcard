const wallet = require("../src/wallet");

async function test(){ 
    await wallet.GetStatus()
    await wallet.VerifyPin("11111111")
    await wallet.ResetWalletWithAuth("11111111")
    await wallet.GetStatus()
}

setTimeout(()=>{
    test()

}, 2000)