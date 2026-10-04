package user;

import javacard.security.*;


import javacard.framework.*;

import javacardx.crypto.*;

public class ECDSA {
	final static byte[] SecP256k1_P = {
			(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
			(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
			(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
			(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFE,(byte)0xFF,(byte)0xFF,(byte)0xFC,(byte)0x2F};
		
	final static byte[] SecP256k1_A = {
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
	
	final static byte[] SecP256k1_B = {
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x07};
	
	final static byte[] SecP256k1_S = {
		(byte)0xC4,(byte)0x9D,(byte)0x36,(byte)0x08,(byte)0x86,(byte)0xE7,(byte)0x04,(byte)0x93,
		(byte)0x6A,(byte)0x66,(byte)0x78,(byte)0xE1,(byte)0x13,(byte)0x9D,(byte)0x26,(byte)0xB7,
		(byte)0x81,(byte)0x9F,(byte)0x7E,(byte)0x90};
	
	// Uncompress form
	final static byte[] SecP256k1_G = {
		(byte)0x04, (byte)0x79, (byte)0xBE, (byte)0x66, (byte)0x7E, (byte)0xF9, (byte)0xDC, (byte)0xBB,
		(byte)0xAC, (byte)0x55, (byte)0xA0, (byte)0x62, (byte)0x95, (byte)0xCE, (byte)0x87, (byte)0x0B, 
		(byte)0x07, (byte)0x02, (byte)0x9B, (byte)0xFC, (byte)0xDB, (byte)0x2D, (byte)0xCE, (byte)0x28, 
		(byte)0xD9, (byte)0x59, (byte)0xF2, (byte)0x81, (byte)0x5B, (byte)0x16, (byte)0xF8, (byte)0x17, 
		(byte)0x98, (byte)0x48, (byte)0x3A, (byte)0xDA, (byte)0x77, (byte)0x26, (byte)0xA3, (byte)0xC4, 
		(byte)0x65, (byte)0x5D, (byte)0xA4, (byte)0xFB, (byte)0xFC, (byte)0x0E, (byte)0x11, (byte)0x08, 
		(byte)0xA8, (byte)0xFD, (byte)0x17, (byte)0xB4, (byte)0x48, (byte)0xA6, (byte)0x85, (byte)0x54, 
		(byte)0x19, (byte)0x9C, (byte)0x47, (byte)0xD0, (byte)0x8F, (byte)0xFB, (byte)0x10, (byte)0xD4, (byte)0xB8};
	
	final static byte[] SecP256K1_GC = {
		(byte)0x02, (byte)0x79, (byte)0xBE, (byte)0x66, (byte)0x7E, (byte)0xF9, (byte)0xDC, (byte)0xBB,
		(byte)0xAC, (byte)0x55, (byte)0xA0, (byte)0x62, (byte)0x95, (byte)0xCE, (byte)0x87, (byte)0x0B,
		(byte)0x07, (byte)0x02, (byte)0x9B, (byte)0xFC, (byte)0xDB, (byte)0x2D, (byte)0xCE, (byte)0x28,
		(byte)0xD9, (byte)0x59, (byte)0xF2, (byte)0x81, (byte)0x5B, (byte)0x16, (byte)0xF8, (byte)0x17, (byte)0x98};
	
	final static byte[] SecP256k1_N = {
		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, 
		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFE, 
		(byte)0xBA, (byte)0xAE, (byte)0xDC, (byte)0xE6, (byte)0xAF, (byte)0x48, (byte)0xA0, (byte)0x3B, 
		(byte)0xBF, (byte)0xD2, (byte)0x5E, (byte)0x8C, (byte)0xD0, (byte)0x36, (byte)0x41, (byte)0x41};
	
	final static short  SecP256k1_H =  1;
	
	public final static short 	EC_Privatekey_Length 				= 32;
	public final static short 	EC_PublicKey_Length 				= 65; 
	public final static short 	Keccak256_Length					= 32;
	public final static short 	ECDSA_Signature_Length 				= 64;
	
	private ECPrivateKey 			objECDSAPriKey;
	private ECPublicKey				objECDSAPubKey;
	private ECPrivateKey 			genuineECDSAPriKey;
	private ECPublicKey				genuineECDSAPubKey;
	private Signature				objECDSASign   			= null;
	private KeyAgreement 			ecPointMultiplier;
	private KeyPair					objECDSAKeyPair;
//	private MessageDigest			sha256;
	

	public void install() {
		objECDSAPriKey = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
		objECDSAPubKey = (ECPublicKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
		
//		sha256 = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
		
		objECDSASign = Signature.getInstance(MessageDigest.ALG_SHA_256, Signature.SIG_CIPHER_ECDSA_PLAIN, Cipher.PAD_NULL, false);
		
		objECDSAPubKey.setFieldFP(SecP256k1_P, (short)0, (short)SecP256k1_P.length);
		objECDSAPubKey.setA(SecP256k1_A, (short)0, (short)SecP256k1_A.length);
		objECDSAPubKey.setB(SecP256k1_B, (short)0, (short)SecP256k1_B.length);
		objECDSAPubKey.setG(SecP256k1_G, (short)0, (short)SecP256k1_G.length);
		objECDSAPubKey.setR(SecP256k1_N, (short)0, (short)SecP256k1_N.length);
		objECDSAPubKey.setK(SecP256k1_H);
		
		objECDSAPriKey.setFieldFP(SecP256k1_P, (short)0, (short)SecP256k1_P.length);
		objECDSAPriKey.setA(SecP256k1_A, (short)0, (short)SecP256k1_A.length);
		objECDSAPriKey.setB(SecP256k1_B, (short)0, (short)SecP256k1_B.length);
		objECDSAPriKey.setG(SecP256k1_G, (short)0, (short)SecP256k1_G.length);
		objECDSAPriKey.setR(SecP256k1_N, (short)0, (short)SecP256k1_N.length);
		objECDSAPriKey.setK(SecP256k1_H);	
		
		ecPointMultiplier = KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH_PLAIN_XY, false);
		
		
		genuineECDSAPriKey = (ECPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_256, false);
		genuineECDSAPubKey = (ECPublicKey)KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
		
		genuineECDSAPubKey.setFieldFP(SecP256k1_P, (short)0, (short)SecP256k1_P.length);
		genuineECDSAPubKey.setA(SecP256k1_A, (short)0, (short)SecP256k1_A.length);
		genuineECDSAPubKey.setB(SecP256k1_B, (short)0, (short)SecP256k1_B.length);
		genuineECDSAPubKey.setG(SecP256k1_G, (short)0, (short)SecP256k1_G.length);
		genuineECDSAPubKey.setR(SecP256k1_N, (short)0, (short)SecP256k1_N.length);
		genuineECDSAPubKey.setK(SecP256k1_H);
		
		genuineECDSAPriKey.setFieldFP(SecP256k1_P, (short)0, (short)SecP256k1_P.length);
		genuineECDSAPriKey.setA(SecP256k1_A, (short)0, (short)SecP256k1_A.length);
		genuineECDSAPriKey.setB(SecP256k1_B, (short)0, (short)SecP256k1_B.length);
		genuineECDSAPriKey.setG(SecP256k1_G, (short)0, (short)SecP256k1_G.length);
		genuineECDSAPriKey.setR(SecP256k1_N, (short)0, (short)SecP256k1_N.length);
		genuineECDSAPriKey.setK(SecP256k1_H);
		
		objECDSAKeyPair = new KeyPair(genuineECDSAPubKey, genuineECDSAPriKey);
		
	}
	

	
    public void SignHash(byte[] hashIn, short hashOff, byte[] signatureOut, short signatureOff, byte[] privateKey, short privateKeyOff){

		try{
			objECDSAPriKey.setS(privateKey, privateKeyOff, EC_Privatekey_Length);
			objECDSASign.init(objECDSAPriKey, Signature.MODE_SIGN);
			objECDSASign.signPreComputedHash(hashIn, hashOff, Keccak256_Length, signatureOut, signatureOff);
		}
		catch(CryptoException ex){
			ISOException.throwIt(ex.getReason());
		}

    }
    
 
    public void derivePublicKey(byte[] privateKey, short priOff, byte[] pubOut, short pubOff) {
    	objECDSAPriKey.setS(privateKey, priOff, EC_Privatekey_Length);
    	ecPointMultiplier.init(objECDSAPriKey);
    	ecPointMultiplier.generateSecret(SecP256k1_G, (short) 0, (short) SecP256k1_G.length, pubOut, pubOff);
    }
    
    public void getGenuinePublicKey(byte[] pubOut, short pubOff) {
    	objECDSAKeyPair.genKeyPair();
    	genuineECDSAPubKey.getW(pubOut, pubOff);
    }
    
    public void signGenuine(byte[] dataIn, short dataOff, short dataLength , byte[] signOut, short signOff) {
    	objECDSASign.init(genuineECDSAPriKey, Signature.MODE_SIGN);
    	objECDSASign.sign(dataIn, dataOff, dataLength, signOut, signOff);
    }
    
}
