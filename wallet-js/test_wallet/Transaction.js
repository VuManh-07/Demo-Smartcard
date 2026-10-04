const wallet = require("../src/wallet.js");
const { ethers } = require("ethers");

class Provider {
  constructor() {
    this.provider = new ethers.providers.JsonRpcProvider(
      "https://goerli.infura.io/v3/eb4e3ec2fd9c4b92ae0142d44172e91e"
    );
    this.tx = {};
  }
  async getGas() {
    const feeData = await this.provider.getFeeData();
    console.log(feeData);
    console.log(feeData.lastBaseFeePerGas._hex);
    console.log(feeData.maxFeePerGas._hex);
    console.log(feeData.maxPriorityFeePerGas._hex);
    console.log(feeData.gasPrice._hex);
    this.tx = { ...feeData };
  }
  getTx() {
    return this.tx;
  }
}

async function test() {
  const provider = new Provider();
  console.log(await provider.getGas());
  console.log(provider.getTx());
  await wallet.GetStatus();
  await wallet.VerifyPin("11111111");
  // console.log("Address:", await wallet.RLP_Decode("02f86c05778477359400847735940e8252089448d047c090d9bc5a6209d7525a313e0b594b1b2680b844a9059cbb000000000000000000000000eef02f7364f0a33e08078864a2b6c42998847dfd000000000000000000000000000000000000000000000000000000000000000ac0"))
  console.log(
    await wallet.Transaction(
      "63fc286b35855b2ed529e1094c7a551d12d263acbde1428b972041434b444cc8",
      "11111111",
      0
    )
  );
}

setTimeout(() => {
  test();
}, 2000);
