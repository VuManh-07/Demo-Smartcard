"use strict";

const publicKeyToAddress = require("ethereum-public-key-to-address");
const utils = require("./Utils");
const SmartCard = require("./SmartCard");
const bip39 = require("bip39");

const GENERATE_SEED = "00";
const IMPORT_SEED = "01";

const RESET_WALLET = "02";
const RESET_WITH_AUTH = "23";

const GET_ACCOUNT = "03";
const GET_ACCOUNT_0 = "04";
const TRANSACTION = "06";

const VERIFY_PIN = "07";
const CHANGE_PIN = "08";

const GET_INFOMATION = "0E";
const GET_STATUS = "0F";

// const IMPORT_ACCOUNT = "16";
const INS_DERIVE_MASTER = "29";
const INS_DERIVE_COIN = "24";
const INS_DERIVE_ACCOUNT = "25";
const INS_DERIVE_CHANGE = "26";
const INS_DERIVE_ADDRESS = "27";
const INS_GENERATE_PUBKEY = "28";

const DELETE_ACCOUNT = "18";

const WalletNonActived = "0001";
const WalletActived = "0002";

/**
 * Get satuts Wallet
 * @returns {Hex String} - Status of Wallet (Example - "9000")
 */
async function GetStatus() {
  var result = await SmartCard.SecureMessage(GET_STATUS);
  if (result.sw == WalletNonActived) console.log("Wallet Non Active");
  else if (result.sw == WalletActived) console.log("Wallet Actived");
  else console.log("Wallet Non Install");

  return result.sw;
}

/**
 * Get Infomation Wallet
 * @returns {Object} Infomation Wallet - {"id": "",
 *                                        "date": "",
 *                                        "version": "",
 *                                        "type":"" }
 */
async function GetInfomation() {
  var result = await SmartCard.SecureMessage(GET_INFOMATION);
  var data = utils.HexString_To_String(result.data);
  console.log(data);
  return data;
}

/**
 * Import Seed Root to Wallet
 * @param {Hex String} mnemonic - seed root of HDWallet
 */
async function ImportMemonicWord(mnemonic) {
  var seed = bip39.mnemonicToSeedSync(mnemonic.trim()).toString("hex");
  var result = await SmartCard.SecureMessage(IMPORT_SEED, seed);
  if (result.sw == "9000") return true;
  else return false;
}

/**
 * Generate Entropy for HD Wallet
 * @returns {Hex String} - Result perform function
 */
async function GenerateHDWallet() {
  var result = await SmartCard.SecureMessage(GENERATE_SEED);
  if (result.sw != "9000") {
    console.log("Cannot Generate Seed Root!");
    return;
  }
  var mnemonic = bip39.entropyToMnemonic(result.data);
  return mnemonic;
  // if(await ImportMemonicWord(mnemonic) == true)
  //     return mnemonic
  // else
  //     return
}

/**
 * Get Account in Wallet
 * @param {Unsigned Int} index - Account Number in Wallet
 * @returns {Object} - {pub: PublicKeyValue, addr: AddressValue, index: indexAccount}
 */
async function GetAccount(index) {
  index = utils.Int_To_HexString(index);
  var result_1 = await SmartCard.SecureMessage(GET_ACCOUNT, null, index);
  if (result_1.sw == "9000") {
    try {
      var address = publicKeyToAddress(result_1.data);
    } catch {
      console.log("Get Acount Error: Cannot Convert Address from Public Key!");
      return;
    }
    return {
      publickey: result_1.data,
      address: address,
      index: utils.HexString_To_Int(index),
    };
  } else return;
}


async function GetAccount0() {
  // index = utils.Int_To_HexString(index);
  var result_1 = await SmartCard.SecureMessage(GET_ACCOUNT_0, null);
  if (result_1.sw == "9000") {
    try {
      var address = publicKeyToAddress(result_1.data);
    } catch {
      console.log("Get Acount Error: Cannot Convert Address from Public Key!");
      return;
    }
    return {
      publickey: result_1.data,
      address: address,
      index: 0,
    };
  } else return;
}

/**
 *
 * @param {String} mnemonic
 * @returns true or false
 */
async function ResetWallet(mnemonic) {
  // var entropy = bip39.mnemonicToEntropy(mnemonic.trim());
  // var seed = bip39.mnemonicToSeedSync(mnemonic.trim());
  var result = await SmartCard.SecureMessage(RESET_WALLET, mnemonic);
  if (result.sw == "9000") return true;
  else return false;
}

async function ResetWalletWithAuth(pin = "11111111") {
  pin = utils.String_To_HexString(pin);
  var result = await SmartCard.SecureMessage(RESET_WITH_AUTH, pin);

  if (result.sw == "9000") return true;
  else return false;
}

/**
 * Import Account into Wallet
 * @param {Hex String} privatekey: Private Key of Account
 * @param {Integer} index: Account Index
 * @returns true or false
 */
async function GenerateAccount(index) {
  index = utils.Int_To_HexString(index);
  await SmartCard.SecureMessage(INS_DERIVE_MASTER, null, index);
  await SmartCard.SecureMessage(INS_DERIVE_COIN, null, index);
  await SmartCard.SecureMessage(INS_DERIVE_ACCOUNT, null, index);
  await SmartCard.SecureMessage(INS_DERIVE_CHANGE, null, index);
  await SmartCard.SecureMessage(INS_DERIVE_ADDRESS, null, index);
  await SmartCard.SecureMessage(INS_GENERATE_PUBKEY, null, index);
  var result_1 = await SmartCard.SecureMessage(GET_ACCOUNT, null, index);
  if (result_1.sw == "9000") {
    try {
      var address = publicKeyToAddress(result_1.data);
    } catch {
      console.log("Get Acount Error: Cannot Convert Address from Public Key!");
      return;
    }
    return {
      publickey: result_1.data,
      address: address,
      index: utils.HexString_To_Int(index),
    };
  } else return;
}

/**
 * Delete Account in Wallet
 * @param {Integer} index: Account Index
 * @returns true of false
 */
async function DeleteAccount(index) {
  index = utils.Int_To_HexString(index);
  var result = await SmartCard.SecureMessage(DELETE_ACCOUNT, null, index);
  if (result.sw == "9000") return true;
  else return false;
}

/**
 * Verify PIN code Wallet
 * @param {String} pin
 * @returns {Boolean} - Status excutive APDU command
 */
async function VerifyPin(pin) {
  pin = utils.String_To_HexString(pin);
  var result = await SmartCard.SecureMessage(VERIFY_PIN, pin);
  // console.log(result.data)
  if (result.sw == "9000")
    return { verify: true, times: utils.HexString_To_Int(result.data) };
  else return { verify: false, times: utils.HexString_To_Int(result.data) };
}

/**
 * Change Pin code
 * @param {String} oldPin
 * @param {String} newPin
 * @returns {Boolean} - Status excutive APDU command
 */
async function ChangePin(oldPin, newPin) {
  oldPin = utils.String_To_HexString(oldPin);
  newPin = utils.String_To_HexString(newPin);

  console.log(newPin + oldPin);

  var result = await SmartCard.SecureMessage(CHANGE_PIN, newPin + oldPin);
  if (result.sw == "9000") return true;
  else return false;
}

/**
 *
 * @param {String} pin - pin code of Wallet
 * @returns {Object} - {r:"", s:""}
 */
async function Transaction(hash, pin, index) {
  index = utils.Int_To_HexString(index);
  pin = utils.String_To_HexString(pin);
  var result = await SmartCard.SecureMessage(TRANSACTION, index + pin + hash);
  if (result.sw == "9000") {
    return {
      r: result.data.slice(0, 64),
      s: result.data.slice(64, 128),
    };
  } else return;
}

module.exports = {
  GetStatus,
  GetInfomation,
  GetAccount,
  GetAccount0,
  GenerateHDWallet,
  ImportMemonicWord,
  GenerateAccount,
  DeleteAccount,
  VerifyPin,
  ChangePin,
  Transaction,
  ResetWallet,
  ResetWalletWithAuth,
};
