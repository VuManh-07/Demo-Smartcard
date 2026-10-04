const HDWallet = require('ethereum-hdwallet')
const bip39 = require('bip39')

const mnemonic = 'glory cause foot pretty guide trumpet vessel document undo maze old sting inquiry nose gravity attack security exclude combine belt million verify toilet shock'

try {
    var seed = bip39.mnemonicToSeedSync(mnemonic.trim());
    console.log("Seed Root:", seed.toString('hex'));
} catch (error) {
    console.log(error);
}