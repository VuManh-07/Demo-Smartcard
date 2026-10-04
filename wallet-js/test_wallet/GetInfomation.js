const wallet = require("../src/wallet.js");

async function test(){ 
    await wallet.GetStatus()
    await wallet.GetInfomation()
}

setTimeout(()=>{
    test()

}, 2000)