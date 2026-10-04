'use strict';

const smartcard = require('smartcard');
const scp03 = require("./scp03")

const Devices = smartcard.Devices;
const Iso7816Application = smartcard.Iso7816Application;

const devices = new Devices();

const SELECTE                   = "00A40400"
const INITIALIZE_UPDATE         = "80503000"
// const INITIALIZE_UPDATE         = "80500100"
const EXTERNAL_AUTHENTICATE     = "80823300"
let AID                         = "A00001020304"

let card;

devices.on('device-activated', event => {
    let device = event.device;

    device.on('card-inserted', event => {
        card = event.card;
        console.log(`Card '${card.getAtr()}' inserted into '${event.device}'`);
        setTimeout(()=> 
            SecureChannel()
        , 500);       
        
    });       
    
    device.on('card-removed', event => {
        console.log(`Card removed from '${event.name}' `);
        scp03.Reset()
        card = null
    });
});



/**
 *      Initialize Secure Channel (SCP03)
 */
async function SecureChannel(){
    /**
     *  Selecte Secure Domain Command
     */
    const application = new Iso7816Application(card);
    scp03.ImportAID(AID)
    await application.issueCommand(Buffer.from(SELECTE + "06" + AID, 'hex'))
    .then(response => {
        console.info(`Select Secure Domain: '${response}' '${response.meaning()}'`);
    })
    .catch(error => {
        console.error('Error:', error, error.stack);
    });


    /**
     *  INITIALIZE UPDATE Command
     */
    await application.issueCommand(Buffer.from(INITIALIZE_UPDATE + "08" + await scp03.HostChallenge(), 'hex'))
    .then( async response => {
        console.info(`INITIALIZE UPDATE: '${response}' '${response.meaning()}'`);
        await scp03.InitHostSCP03(response["data"].slice(0, 64))       
    })
    .catch(error => {
        console.error('Error:', error, error.stack);
    });


    /**
     * CMAC Generation for EXTENAL AUTHENTICATE Command
     */
     var _hostCryptogram = await scp03.HostCryptogram()
     var _cmdAuth = await scp03.GenerateCMAC(EXTERNAL_AUTHENTICATE, _hostCryptogram.slice(0, 16))


    /**
     *  EXTENAL AUTHENTICATE Command
     */
    return await application.issueCommand(Buffer.from(_cmdAuth, 'hex'))
    .then(async response => {
        console.info(`EXTERNAL_AUTHENTICATE: '${response}' '${response.meaning()}'`);
        scp03.ChannelFinish(response["data"])
        return scp03.isEnableSecureChannel;
    })
    .catch(error => {
        console.error('Error:', error, error.stack);
    });

}


/**
 * 
 * @param {Hex String} INS 
 * @param {Hex String} data 
 * @param {Hex String} P1 
 * @param {Hex String} P2 
 * @returns APDU Response data {data:"", sw:""}
 */
async function SecureMessage(INS, data=null, P1="00", P2="00"){
    console.log();
    const application = new Iso7816Application(card);

    // Wrap APDU command and data
    var command = await scp03.Wrap("B0" + INS + P1 + P2, data)
    
    var t1 = Date.now()
    // Send APDU command
    return await application.issueCommand(Buffer.from(command, 'hex'))  
    .then(async response => {    

        // ADPU Response data
        var result = response['data']  
        var t2 = Date.now()

        // Timer caculation 
        result = await scp03.Unwrap(result)   
        console.log("Time:", t2 - t1, " milis");
        return result
    })
    .catch(error => {
        console.error('Error:', error, error.stack);
    });

}

function SetAID(aid){
    AID = aid
}

function isSecureChannel(){
    return scp03.isEnableSecureChannel()
}

module.exports = {
    SecureChannel,
    SecureMessage,
    SetAID,
    isSecureChannel
}
