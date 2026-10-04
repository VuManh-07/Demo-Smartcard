const wallet = require("../src/wallet");

async function test() {
  console.log(await wallet.GetStatus());
  console.log(await wallet.VerifyPin("12345678"));
  for (var i = 0; i < 5; i++) {
    var index = i;
    console.log("account", await wallet.GetAccount(index));
  }
}

async function test1() {
  console.log(await wallet.GetStatus());
  console.log(await wallet.VerifyPin("11111111"));

  console.log("account", await wallet.GenerateAccount(1));
}

async function test2() {
  console.log(await wallet.GetStatus());

  console.log("account", await wallet.GetAccount0());
}

setTimeout(() => {
  test2();
}, 2000);
