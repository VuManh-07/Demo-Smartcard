(async () => {
    const AesCmac = require('aes-cmac').AesCmac;
  
    const key = Buffer.from('404142434445464748494A4B4C4D4E4F', 'hex');
    const msg = Buffer.from('0000000000000000000000040000800100000eA000000151000000', 'hex');
  
    const aesCmac = new AesCmac(key);
    const result = Buffer.from(await aesCmac.calculate(msg));
    console.log(result.toString('hex'));
  })();