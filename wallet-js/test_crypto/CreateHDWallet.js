const HDWallet = require('ethereum-hdwallet')
const bip39 = require('bip39')
const publicKeyToAddress = require('ethereum-public-key-to-address');

// const mnemonic = 'ball pony spring senior venture blouse drama urge balance arm mosquito morning coyote salmon live essay shiver melt recall volume chicken kiss hint grit'
// const hdwallet = HDWallet.fromMnemonic(mnemonic)


const seed = "ee34898c985eea3969e3035eca92cd764e6b0f0861c1dcc5b3f13227197decc9"
// const hdwallet = HDWallet.fromSeed(seed)
// var seed = bip39.mnemonicToSeedSync(mnemonic.trim());
// console.log("Seed Root:", seed.toString('hex'));
const hdwallet = HDWallet.fromSeed(seed)


const mywallet = hdwallet.derive(`m/44'/60'/0'/0`)
console.log(`Account 1: 0x${mywallet.derive(0).getAddress().toString('hex')}`)
console.log(`Account 2: 0x${mywallet.derive(1).getAddress().toString('hex')}`) 
console.log(`Account 3: 0x${mywallet.derive(2).getAddress().toString('hex')}`) 
console.log(`Account 4: 0x${mywallet.derive(3).getAddress().toString('hex')}`) 
console.log(`Account 5: 0x${mywallet.derive(4).getAddress().toString('hex')}`) 
console.log(`Account 6: 0x${mywallet.derive(5).getAddress().toString('hex')}`) 
console.log(`Account 7: 0x${mywallet.derive(6).getAddress().toString('hex')}`) 
console.log(`Account 8: 0x${mywallet.derive(7).getAddress().toString('hex')}`) 
console.log(`Account 9: 0x${mywallet.derive(8).getAddress().toString('hex')}`) 
console.log(`Account 10: 0x${mywallet.derive(9).getAddress().toString('hex')}`) 

// console.log("Account 1:", mywallet.derive(0).getPublicKey().toString('hex'));
// console.log("Account 2:", mywallet.derive(1).getPublicKey().toString('hex'));
// console.log(`0x${mywallet.derive(3).hdpath()}`) // m/44'/60'/0'/0/3
