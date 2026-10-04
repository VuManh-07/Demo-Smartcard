const wallet = require("../src/wallet.js");

async function test(){ 
    await wallet.GetStatus()
    await wallet.VerifyPin("11111111")
    await wallet.ChangePin("11111111", "22222222")
}

setTimeout(()=>{
    test()

}, 2000)