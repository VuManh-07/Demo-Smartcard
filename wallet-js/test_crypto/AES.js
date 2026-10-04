var CryptoJS = require("crypto-js");

var key = CryptoJS.enc.Hex.parse("41d900937991b8381488e62e42a09794");
var iv = CryptoJS.enc.Hex.parse("80000000000000000000000000000001");
var src = CryptoJS.enc.Hex.parse("616263616263616263616263616263616263616263616263616263")

var encrypted = CryptoJS.AES.encrypt(src, key, {iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Iso97971}).ciphertext.toString();

console.log(encrypted);


var src1 = "58af17cc3a5975a61ac8bd637cbd739d"
var decrypted = CryptoJS.AES.decrypt(src1, key, {iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Iso97971, format: CryptoJS.format.Hex}).toString();
console.log(decrypted);