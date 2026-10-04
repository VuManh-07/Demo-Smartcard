const HDWallet = require('ethereum-hdwallet')
const bip39 = require('bip39')


let entropy = "ee34898c985eea3969e3035eca92cd764e6b0f0861c1dcc5b3f13227197decc9"
var mnemonic = bip39.entropyToMnemonic(entropy)
seed = bip39.mnemonicToSeedSync(mnemonic.trim());
console.log("Seed Root:", seed.toString('hex'));
console.log(mnemonic);