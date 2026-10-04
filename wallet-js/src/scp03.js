const AesCmac = require('aes-cmac').AesCmac;
var CryptoJS = require("crypto-js");

let CardSCP03 = {
    keyDiversification: null,
    keyInfomation: null,
    cardChallenge: null,
    cardCryptogram: null,
    sequenceCounter: null,
    aid: "A00001020304"
}

let HostSCP03 = {
    hostChallenge: null,
    cardChallenge:null,
    cardCryptogram:null,
    hostCryptogram:null,
    MacChaining:"00000000000000000000000000000000",
    S_MAC:null,
    S_ENC:null,
    S_RMAC:null,
    Key_MAC:"404142434445464748494A4B4C4D4E4F",
    Key_ENC:"404142434445464748494A4B4C4D4E4F",
    Key_DEK:"404142434445464748494A4B4C4D4E4F",
    FUNC_SMAC:null,
    FUNC_SENC:null,
    FUNC_RMAC:null,
    ICV:null,
    EncryptCounter: 0
}

let SCP03Success = false;
let aesCmac_enc;
let aesCmac_mac
let aesCmac_smac;
let cardChallengeFlag = false
let cardCryptogramFlag = false

// function InitCard()

function Reset(){
    SCP03Success = false
    cardChallengeFlag = false
    cardCryptogramFlag = false

    CardSCP03.keyDiversification    = null;
    CardSCP03.keyInfomation         = null;
    CardSCP03.cardChallenge         = null;
    CardSCP03.cardCryptogram        = null;
    CardSCP03.sequenceCounter       = null;

    HostSCP03.MacChaining           = "00000000000000000000000000000000";
    HostSCP03.hostChallenge         = null;
    HostSCP03.cardChallenge         = null;
    HostSCP03.cardCryptogram        = null;
    HostSCP03.hostCryptogram        = null;
    HostSCP03.S_ENC                 = null;
    HostSCP03.S_ENC                 = null;
    HostSCP03.S_RMAC                = null;
    HostSCP03.FUNC_SMAC             = null;
    HostSCP03.FUNC_SENC             = null;
    HostSCP03.FUNC_RMAC             = null;
    HostSCP03.ICV                   = null;
    HostSCP03.EncryptCounter        = 0;

    console.log("SCP03 Reset");
}

async function SessionKey(){
    /**
     * Calculator Session Key
     */
    aesCmac_enc = new AesCmac(Buffer.from(HostSCP03.Key_ENC,"hex"));
    HostSCP03.S_ENC = await aesCmac_enc.calculate(Buffer.from( DataDerivationScheme("04", "0080", HostSCP03.hostChallenge, CardSCP03.cardChallenge) ,"hex"))
    
    aesCmac_mac = new AesCmac(Buffer.from(HostSCP03.Key_MAC,"hex"));
    HostSCP03.S_MAC = await aesCmac_mac.calculate(Buffer.from( DataDerivationScheme("06", "0080", HostSCP03.hostChallenge, CardSCP03.cardChallenge) ,"hex"))
    HostSCP03.S_RMAC = await aesCmac_mac.calculate(Buffer.from( DataDerivationScheme("07", "0080", HostSCP03.hostChallenge, CardSCP03.cardChallenge) ,"hex"))

    HostSCP03.S_ENC = Buffer.from(HostSCP03.S_ENC).toString("hex")
    HostSCP03.S_MAC = Buffer.from(HostSCP03.S_MAC).toString("hex")
    HostSCP03.S_RMAC = Buffer.from(HostSCP03.S_RMAC).toString("hex")

    HostSCP03.FUNC_SENC = new AesCmac(Buffer.from(HostSCP03.S_ENC,"hex"));
    HostSCP03.FUNC_SMAC = new AesCmac(Buffer.from(HostSCP03.S_MAC,"hex"));
    HostSCP03.FUNC_RMAC = new AesCmac(Buffer.from(HostSCP03.S_RMAC,"hex"));

    // console.log("S-ENC:", HostSCP03.S_ENC);
    // console.log("S-MAC:", HostSCP03.S_MAC);
    // console.log("S-RMAC:", HostSCP03.S_RMAC);
}

async function CompareCardChallenge(){
    /**
     * Calculator Card Challenge from Host
     */
    aesCmac_enc = new AesCmac(Buffer.from(HostSCP03.Key_ENC,"hex"));
    HostSCP03.cardChallenge = await aesCmac_enc.calculate(Buffer.from(DataDerivationScheme("02", "0040", CardSCP03.sequenceCounter, CardSCP03.aid) ,"hex"))
    HostSCP03.cardChallenge = Buffer.from(HostSCP03.cardChallenge).toString("hex")
    // console.log("Compare Card Challenge:", HostSCP03.cardChallenge, "---", CardSCP03.cardChallenge);

    /**
     * Compare Card Challenge
     */
    if(HostSCP03.cardChallenge.slice(0, 16) == CardSCP03.cardChallenge){
        cardChallengeFlag = true
        console.log("Card Challenge Valid");
    }
    else{
        cardChallengeFlag = false
        console.log("Card Challenge Invalid");
    }
        
}

async function CompareCardCryptogram(){
    /**
     * Calculator Card Cryptogram from Host
     */
    aesCmac_smac = new AesCmac(Buffer.from(HostSCP03.S_MAC,"hex"));
    HostSCP03.cardCryptogram = await aesCmac_smac.calculate(Buffer.from( DataDerivationScheme("00", "0040", HostSCP03.hostChallenge, CardSCP03.cardChallenge) ,"hex"))
    HostSCP03.cardCryptogram = Buffer.from(HostSCP03.cardCryptogram).toString("hex")
    // console.log("Compare Card Cryptogram:", HostSCP03.cardCryptogram, "---", CardSCP03.cardCryptogram);

    /**
     * Compare Card Cryptogram
     */
    if(HostSCP03.cardCryptogram.slice(0, 16) == CardSCP03.cardCryptogram){
        cardCryptogramFlag = true;
        console.log("Card Cryptogram Valid");
    }        
    else{
        cardCryptogramFlag = false;
        console.log("Card Cryptogram Invalid");
    }
}
        
async function HostCryptogram(){
    /**
     * Calculator Host Cryptogram
     */
    aesCmac_smac = new AesCmac(Buffer.from(HostSCP03.S_MAC,"hex"));
    HostSCP03.hostCryptogram = await aesCmac_smac.calculate(Buffer.from( DataDerivationScheme("01", "0040", HostSCP03.hostChallenge, CardSCP03.cardChallenge) ,"hex"))
    HostSCP03.hostCryptogram = Buffer.from(HostSCP03.hostCryptogram).toString("hex")
    // console.log("Host Cryptogram:", HostSCP03.hostCryptogram)
    return HostSCP03.hostCryptogram
}

async function Wrap(command, data){
    HostSCP03.EncryptCounter += 1
    if(data != null)
        data = Encrypt(data)
    else 
        data = ""
    return await GenerateCMAC(command, data)
}


async function Unwrap(response){
    console.log("Respone:", response);
    var dataSplit = SplitResponse(response)

    // console.log(dataSplit);


    if(await VerifyRMAC(dataSplit.data, dataSplit.rmac, dataSplit.sw) == false){
        return dataSplit
    }  
    
    if(dataSplit.data != "")
        dataSplit.data = Decrypt(dataSplit.data)
    
    return dataSplit
}

async function GenerateCMAC(command, data){

    var CLA     = command.slice(0, 2)
    var P       = command.slice(2, 8)
    var Lc      = data.length / 2


    CLA = (parseInt(CLA, 16) + 4).toString(16) 
    Lc = (Lc + 8).toString(16)
    
    CLA = CLA.padStart(2, "0")
    Lc = Lc.padStart(2, "0")

    var _cmd  = CLA + P + Lc + data
    // console.log(_cmd);
    // console.log("MacChining:", HostSCP03.MacChaining);
    // HostSCP03.FUNC_SMAC = new AesCmac(Buffer.from(HostSCP03.S_MAC,"hex"));
    HostSCP03.MacChaining = await HostSCP03.FUNC_SMAC.calculate(Buffer.from(HostSCP03.MacChaining + _cmd, "hex"))
    HostSCP03.MacChaining = Buffer.from(HostSCP03.MacChaining).toString("hex")
    
    console.log("Command:", _cmd + HostSCP03.MacChaining.slice(0, 16));
   
    return _cmd + HostSCP03.MacChaining.slice(0, 16)
}

async function HostChallenge(){
    var dataInt = Math.floor(Math.random() * 1000000000000000)
    var dataHex = dataInt.toString(16)
    dataHex = dataHex.padStart(16, "0")
    HostSCP03.hostChallenge = dataHex
    return dataHex
}

function SplitResponse(response){
    
    var len;
    if( response.length == 20 )
        len = 0
    else if( response.length > 50)
        len = response.length - 4 - 16
    else
        return false

    // console.log("len:", len,  response.length);
    /**
     * Split Response Data, RMAC, SW
     */
    var data;
    if(len == 0)
        data = "";
    else
        data = response.slice(0, len)
    var rmac = response.slice(len, len + 16)
    var sw = response.slice(len + 16, len + 20)
    return {data: data, rmac: rmac, sw:sw}
}

async function VerifyRMAC(data, rmac, sw){
    if(SCP03Success == false)
        return false
    /**
     * Calculator RMAC from host
     */
    

    var _rmac = await HostSCP03.FUNC_RMAC.calculate(Buffer.from(HostSCP03.MacChaining + data + sw, "hex"))
    _rmac = Buffer.from(_rmac).toString("hex").slice(0, 16)
    // console.log("RMAC:", _rmac, "-", rmac);

    /**
     * Compare RMAC
     */
    if(_rmac == rmac){
        // console.log("RMAC Valid");
        return true
    }        
    else{
        // console.log("RMAC Invalid");
        Reset()
        return false
    }
        
}

function Encrypt(data){

    if(SCP03Success == false)
        return false

    // console.log(data);
    var key = CryptoJS.enc.Hex.parse(HostSCP03.S_ENC);

    /**
     * Calculator Vector V
     */
    var _EnCounter = HostSCP03.EncryptCounter.toString(16)
    _EnCounter = _EnCounter.padStart(32, "0")
    // console.log("Vector IV", _EnCounter);
    
    _EnCounter = CryptoJS.enc.Hex.parse(_EnCounter)
    HostSCP03.ICV = CryptoJS.AES.encrypt(_EnCounter, key, {iv: CryptoJS.enc.Hex.parse("00000000000000000000000000000000"), mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.NoPadding}).ciphertext.toString();

    
    /**
     * Calculator Encrypt Data
     */    
    HostSCP03.ICV = CryptoJS.enc.Hex.parse(HostSCP03.ICV);
    data = CryptoJS.enc.Hex.parse(data)
    var encrypted = CryptoJS.AES.encrypt(data, key, {iv: HostSCP03.ICV, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Iso97971}).ciphertext.toString();
    return encrypted
}

function Decrypt(data){
    if(SCP03Success == false)
        return false

    var key = CryptoJS.enc.Hex.parse(HostSCP03.S_ENC);
    /**
     * Calculator Vector V
     */
    var _EnCounter = HostSCP03.EncryptCounter.toString(16)
    _EnCounter = _EnCounter.padStart(30, "0")
    _EnCounter = _EnCounter.padStart(32, "80")

    // console.log("Vector IV", _EnCounter);
    
    _EnCounter = CryptoJS.enc.Hex.parse(_EnCounter)
    HostSCP03.ICV = CryptoJS.AES.encrypt(_EnCounter, key, {iv: CryptoJS.enc.Hex.parse("00000000000000000000000000000000"), mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.NoPadding}).ciphertext.toString();
    HostSCP03.ICV = CryptoJS.enc.Hex.parse(HostSCP03.ICV);
    // console.log("Data for Decrypt:", data);

    var decrypted = CryptoJS.AES.decrypt(data, key, {iv: HostSCP03.ICV, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Iso97971, format: CryptoJS.format.Hex}).toString();
    // console.log("Decrypt Data:",  decrypted);
    return decrypted
}

function DataDerivationScheme(derivationConstant, L , context_1, context_2){
    var label = "0000000000000000000000" + derivationConstant
    var separationIndicator = "00"
    var i = "01"
    var context = context_1 + context_2
    return label + separationIndicator + L + i +context
}

async function InitHostSCP03(cardParam){
    /**
     * Read Parameter SCP03 from SmartCard
     */
    CardSCP03.keyDiversification     = await cardParam.slice(0, 20)
    CardSCP03.keyInfomation          = await cardParam.slice(20, 26)
    CardSCP03.cardChallenge          = await cardParam.slice(26, 42)
    CardSCP03.cardCryptogram         = await cardParam.slice(42, 58)
    CardSCP03.sequenceCounter        = await cardParam.slice(58, 64)

    /*
    *  Calculator Session key 
    */

    await SessionKey()
    await CompareCardChallenge()
    await CompareCardCryptogram()
}

function ChannelFinish(authResponse){
    if(authResponse == "9000" & cardChallengeFlag == true & cardCryptogramFlag == true) {
        SCP03Success = true
        console.log("Init Secure Channel Success");
    }
    else{
        SCP03Success = false
        console.log("Init Secure Channel Failure");
    }
        

}

function isEnableSecureChannel(){
    return SCP03Success
}

function ImportAID(aid){
    CardSCP03.aid = aid
}

module.exports = { 
    InitHostSCP03, 
    HostCryptogram, 
    Wrap, 
    Unwrap, 
    HostChallenge, 
    ChannelFinish, 
    Reset, 
    GenerateCMAC, 
    isEnableSecureChannel,
    ImportAID
}