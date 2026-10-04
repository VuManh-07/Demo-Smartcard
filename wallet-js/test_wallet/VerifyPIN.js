const wallet = require("../src/wallet.js");

async function test() {
  await wallet.GetStatus();
  // var result_1 = await wallet.VerifyPin("11111111");
  // console.log(result_1);

  // var result_2 = await wallet.ChangePin("11111111", "12345678");
  // console.log(result_2);

//   var result_3 = await wallet.VerifyPin("12345678");
//   console.log(result_3);
}

setTimeout(() => {
  test();
}, 2000);
