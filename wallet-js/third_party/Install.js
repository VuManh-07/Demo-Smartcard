"use strict";
// const publicKeyToAddress = require('ethereum-public-key-to-address');

const utils = require("../src/Utils");
const SmartCard = require("../src/SmartCard");
const axios = require("axios");
const { GetAccount, VerifyPin } = require("../src/wallet");
const { v4: uuidv4 } = require("uuid");
const wallet = require("../src/wallet.js");

const baseUrl = "http://13.213.33.242:3000/node1/v2/api/";

const axiosInstance = axios.create({
  baseURL: baseUrl,
  timeout: 10000, // (milliseconds)
  headers: {
    "Content-Type": "application/json",
    "secret-key":
      "34c0f5406a0f9265dd6889ab4b156dae1c14816f55462d82c7ba6fe965256a34",
    "x-forwarded-for": "111.222.333",
  },
});

const IMPORT_INFO = "0D";
const IMPORT_SEED = "01";
const GET_STATUS = "0F";

async function GetStatus() {
  var result = await SmartCard.SecureMessage(GET_STATUS);
  console.log("Get Status:", result);
}

async function ImportSEED(seed) {
  var result1 = await SmartCard.SecureMessage(IMPORT_SEED, seed);
  console.log("Import Seed 1 root:", result1);
}

async function ImportInfomation(infomation) {
  // var signature = utils.SignECDSA(infomation, privatekey)
  console.log(infomation);
  infomation = utils.String_To_HexString(infomation);
  var result = await SmartCard.SecureMessage(IMPORT_INFO, infomation);
  console.log("Import Infomation Card:", result);
}

async function createCardInDB(code) {
  try {
    const response = await axiosInstance.post("card/admin/create_card", {
      type: "EVM Compatible",
      DS_Type: "ECDSA",
      code,
      role: "shop",
    });
    console.log(response.data.metadata);
    return;
  } catch (error) {
    throw error;
  }
}

async function importAccountInDB(code) {
  try {
    var result_1 = await wallet.VerifyPin("11111111");
    console.log(result_1);
    var result_2 = await wallet.ChangePin("11111111", "12345678");
    console.log(result_2);
    var result_1 = await wallet.VerifyPin("12345678");
    console.log(result_1);

    const _account = await GetAccount(0);
    const _account1 = await wallet.GenerateAccount(1);
    console.log({ _account1 });
    if (!_account?.address) return;
    const account = {
      name: `Account 1`,
      address: _account.address,
      publickey: _account.publickey,
      index: _account.index,
      type: "default",
    };

    console.log("account", account);

    const response = await axiosInstance.post(`card/user/active_card`, {
      dataAccount: account,
      code,
    });
    console.log(response.data);
    return;
  } catch (error) {
    throw error;
  }
}

setTimeout(async () => {
  await GetStatus();
  const code = uuidv4();
  // console.log({ code });
  // await createCardInDB(code);
  await ImportInfomation(code);
  // await ImportSEED(
  //   "d955927f877fbd0ba5c815d1bd006c272539d4bbc5d5a70ff58bef5567b9fb35f3171356bf22017e20ea9bb91050aed6eb06a0bdc004310c152f10e5334fd59a"
  // );
  // await wallet.GenerateAccount(0);
  await GetStatus();
  // var result_1 = await wallet.VerifyPin("11111111");
  // console.log(result_1);
  // var result_2 = await wallet.ChangePin("11111111", "12345678");
  // console.log(result_2);
  // await importAccountInDB(code);
  // await GetStatus();
  // import info user - account index 0
  console.log("success install");
}, 1000);
