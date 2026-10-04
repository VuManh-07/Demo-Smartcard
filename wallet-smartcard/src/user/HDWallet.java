package user;

import com.nxp.id.jcopx.math.Math;
//import com.nxp.id.jcopx.security.CryptoBaseX;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.RandomData;

public class HDWallet {
	private RandomData 				random;
	private HmacSHA512 				hmacsh512 			= new HmacSHA512();
	
	private final short				ENTROPY_LENGHT		= 32;
	private final short 			CHAIN_LENGHT		= 32;
	private final short 			PRIVATE_LENGHT		= 32;
	
	private byte[]					BtcSeed				= {'B', 'i', 't', 'c', 'o', 'i', 'n', ' ', 's', 'e', 'e', 'd'};
	private byte[] 					seed;
	private short					seed_length;
	private byte[]					entropy;
	
	private byte[]					masterPriCache		= new byte[32];
	private byte[]					masterPubCache		= new byte[65];
	private byte[]					masterChainCache	= new byte[32];
	
	private byte[]					childPriCache	;
	private byte[]					childChainCache	;
	private byte[]					hashCache;
	private boolean					accountSelected		= false;
	private boolean					rlpReceived			= false;
	
	private byte[] purpose;
	private byte[] coin;
	private byte[] account;
	private byte[] change;
	private byte[] address;
	
	private byte[] hmac_in;
	private byte[] hmac_out;
	private byte[] hmacdata;
	private byte[] parentPubKey;
	private byte first;
	private byte itemLength;
	
	private byte[] privateKey = new byte[640];
	private byte[] publicKey  = new byte[1300];


	
	private ECDSA 					ecdsa 				= new ECDSA();
//	private	Keccak					keccak				= new Keccak(); 
	public void install() {
		random = RandomData.getInstance(RandomData.ALG_KEYGENERATION);
		hmacsh512.install();
		ecdsa.install();
		
		childPriCache = JCSystem.makeTransientByteArray(PRIVATE_LENGHT, JCSystem.CLEAR_ON_RESET);
		childChainCache = JCSystem.makeTransientByteArray(PRIVATE_LENGHT, JCSystem.CLEAR_ON_RESET);
		hashCache = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_RESET);
		
		purpose = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
		coin = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
		account = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
		change = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
		address = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
		
		hmacdata = JCSystem.makeTransientByteArray((short) 64, JCSystem.CLEAR_ON_RESET);
		hmac_in = JCSystem.makeTransientByteArray((short) (33+4), JCSystem.CLEAR_ON_RESET);
		hmac_out = JCSystem.makeTransientByteArray((short) (64), JCSystem.CLEAR_ON_RESET);
		parentPubKey = JCSystem.makeTransientByteArray((short) (65), JCSystem.CLEAR_ON_RESET);
	}
	
	/**
	 * 
	 * @param seedIn
	 * @param seedOff
	 * @param seedLength
	 */
	public void ImportSeedRoot(byte[] seedIn, short seedOff, byte seedLength) {
		seed = new byte[seedLength];
		seed_length = seedLength;
		Util.arrayCopyNonAtomic(seedIn, seedOff, seed, (short) 0, seedLength);
		MasterKeyGenerate();
		GenerateAccount();
	}
	
	
	/**
	 * 
	 * @param entropyOut
	 * @param entropyOff
	 * @return
	 */
	public short SeedRootGenerate(byte[] entropyOut, short entropyOff) {
		entropy = new byte[ENTROPY_LENGHT];
		random.nextBytes(entropy, (short)0, ENTROPY_LENGHT);
		Util.arrayCopyNonAtomic(entropy, (short) 0, entropyOut, entropyOff, ENTROPY_LENGHT);
		return ENTROPY_LENGHT;
	}
	
	/**
	 * 
	 */
	public void Reset() {
		Util.arrayFillNonAtomic(privateKey, (short) 0, (short) 640, (byte) 0);
		Util.arrayFillNonAtomic(publicKey, (short)0, (short)1300, (byte)0);
	}
	
	/**
	 * 
	 */
	private void MasterKeyGenerate() {
		Util.arrayFillNonAtomic(hmacdata, (short) 0, (short) 64, (byte)0);
		hmacsh512.hmac(BtcSeed, (short)0, (short)BtcSeed.length, seed, (short)0, seed_length, hmacdata, (short)0);
		
		//		Get Master Private Key From HMAC-SHA512
		Util.arrayCopyNonAtomic(hmacdata, (short)0 , masterPriCache  , (short) 0, (short)PRIVATE_LENGHT);
		
		//		Get Master Chain Code From HMAC-SHA512
		Util.arrayCopyNonAtomic(hmacdata, (short)32, masterChainCache, (short) 0, (short)CHAIN_LENGHT);
		
		//		Get Master Public Key From Master Private Key
		ecdsa.derivePublicKey(masterPriCache, (short) 0, masterPubCache, (short) 0);		
	}
	
	/**
	 * 
	 * @param parentPriKey
	 * @param parentPriOff
	 * @param parentChain
	 * @param parentChainOff
	 * @param index
	 * @param indexOff
	 * @param childPriOut
	 * @param childPriOff
	 * @param childChainOut
	 * @param childChainOff
	 * @param nomal
	 */
	private void ChildKeyGenerate(byte[] parentPriKey, short parentPriOff, byte[] parentChain, short parentChainOff, byte[] index, short indexOff, byte[] childPriOut, short childPriOff, byte[] childChainOut, short childChainOff, boolean nomal) {
		
		Util.arrayFillNonAtomic(hmac_in, (short) 0, (short) (33 + 4), (byte)0);
		Util.arrayFillNonAtomic(hmac_out, (short) 0, (short) 64, (byte)0);
		
		if(nomal == true) {
			Util.arrayFillNonAtomic(parentPubKey, (short) 0, (short) 65, (byte)0);
			//		Derive Public Key From Private Key
			ecdsa.derivePublicKey(parentPriKey, parentPriOff, parentPubKey, (short) 0);
			
			//		Copy Public Key to data-in HMAC 
			Util.arrayCopyNonAtomic(parentPubKey, (short) 0, hmac_in, (short) 0, (short) 33);
			
			//		Copy Index to data-in HMAC
			Util.arrayCopyNonAtomic(index, (short) indexOff, hmac_in, (short) 33, (short) 4);
			
			if(parentPubKey[64] % 2 == 0)
				hmac_in[0] = 0x02;
			else 
				hmac_in[0] = 0x03;
		}
		else {
			Util.arrayCopyNonAtomic(parentPriKey, parentPriOff, hmac_in, (short) 1, PRIVATE_LENGHT);
			Util.arrayCopyNonAtomic(index, (short) indexOff, hmac_in, (short) 33, (short) 4);
		}		
		
		hmacsh512.hmac(parentChain, parentChainOff, CHAIN_LENGHT, hmac_in, (short) 0, (short) hmac_in.length, hmac_out, (short) 0);
		
		//			Get child chain code
		Util.arrayCopyNonAtomic(hmac_out, (short) 32, childChainOut, childChainOff, (short) 32);
		
		//			32 left byte + parent Private Key  % n
		Math.modularAdd(hmac_out, (short) 0, (short) 32, parentPriKey, parentPriOff, (short) 32, ECDSA.SecP256k1_N, (short) 0, (short) 32);
		
		//			Get child private key
		Util.arrayCopyNonAtomic(hmac_out, (short) 0, childPriOut, childPriOff , (short) 32);
 	}
	
	/**
	 * 
	 * @param path
	 * @param pathOff
	 * @param childPriOut
	 * @param childPriOff
	 * @param childChainOut
	 * @param childChainOff
	 */
	private void DeriveFromPath(byte[] path, short pathOff, byte[] childPriOut, short childPriOff, byte[] childChainOut, short childChainOff) {
				
		Util.arrayFillNonAtomic(purpose, (short) 0, (short) 4, (byte)0);
		Util.arrayFillNonAtomic(coin, (short) 0, (short) 4, (byte)0);
		Util.arrayFillNonAtomic(account, (short) 0, (short) 4, (byte)0);
		Util.arrayFillNonAtomic(change, (short) 0, (short) 4, (byte)0);
		Util.arrayFillNonAtomic(address, (short) 0, (short) 4, (byte)0);
		
		Util.arrayCopy(path, pathOff, purpose, (short) 0, (short) 4);
		Util.arrayCopy(path, (short)(pathOff + 4), coin, (short) 0, (short) 4);
		Util.arrayCopy(path, (short)(pathOff + 8), account, (short) 0, (short) 4);
		Util.arrayCopy(path, (short)(pathOff + 12), change, (short) 0, (short) 4);
		Util.arrayCopy(path, (short)(pathOff + 16), address, (short) 0, (short) 4);
		
		ChildKeyGenerate(
				masterPriCache, (short) 0, 
				masterChainCache, (short) 0,
				purpose, (short) 0,
				childPriOut, childPriOff,
				childChainOut, childChainOff, false);
		
		ChildKeyGenerate(
				childPriOut, childPriOff, 
				childChainOut, childChainOff,
				coin, (short) 0,
				childPriOut, childPriOff,
				childChainOut, childChainOff, false);
		
		ChildKeyGenerate(
				childPriOut, childPriOff, 
				childChainOut, childChainOff,
				account, (short) 0,
				childPriOut, childPriOff,
				childChainOut, childChainOff, false);
		
		ChildKeyGenerate(
				childPriOut, childPriOff, 
				childChainOut, childChainOff,
				change, (short) 0,
				childPriOut, childPriOff,
				childChainOut, childChainOff, true);
		
		ChildKeyGenerate(
				childPriOut, childPriOff, 
				childChainOut, childChainOff,
				address, (short) 0,
				childPriOut, childPriOff,
				childChainOut, childChainOff, true);
		
	}
	
	private void ChildPubKey(byte[] childPriKey, short childPriOff, byte[] childPubKey, short childPubOff) {
		ecdsa.derivePublicKey(childPriKey, childPriOff, childPubKey, childPubOff);
	}
	
	/**
	 * 
	 * @param rlpEncode
	 * @param rlpOff
	 * @param rlpLength
	 * @param addressOut
	 * @param addressOff
	 * @return
	 */
	public short RLPDecode(byte[] rlpEncode, short rlpOff, byte rlpLength, byte[] addressOut,short addressOff) {
		
		//		Check first byte of RLP Encode
//		byte first;
		rlpOff += 1;
		first = rlpEncode[rlpOff];
		
		if((byte)(0xC0 - first) > 0xC0) 
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		
		
		if((byte)( first - 0xf7) > 0) 
			rlpOff += 1;
		
		
		//		Calculator Offset of nonce
		rlpOff += 1;
//		
		itemLength = 0x00;
		
		//		Calculator Offset of Gas Price -> Start Gas -> Address
		
		
		for(byte i=0; i<5; i++) {
			
			if((byte)(rlpEncode[rlpOff] - 0x80) > 0) {
				itemLength = (byte)((short)rlpEncode[rlpOff] - (short)0x80);
				rlpOff += itemLength + 1;
			}
			else
				rlpOff += 1;
		}
		
		//		Get Address from RLP Encode
		itemLength = (byte)(rlpEncode[rlpOff] - 0x80);
		rlpOff += 1;	
		Util.arrayCopyNonAtomic(rlpEncode, rlpOff, addressOut, addressOff, itemLength);
		rlpReceived = true;
		return (short)itemLength;
	}
	
	/**
	 * 
	 * @param dataPri
	 * @param offPri
	 * @param lenPri
	 * @param index
	 * @return
	 */
	public boolean ImportAccount(byte[] dataPri, short offPri, short lenPri, byte index) {
		if(index < 10)
			return false;
		if(lenPri != 32)
			return false;
		try {
			Util.arrayCopyNonAtomic(dataPri, offPri, privateKey, (short)(index * 32), lenPri);
			ecdsa.derivePublicKey(privateKey, (short)(index * 32), publicKey, (short)(index * 65));
		}
		catch(Exception ex) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param index
	 */
	public void DeleteAccount(byte index) {
		Util.arrayFillNonAtomic(privateKey, (short)(index * 32), ECDSA.EC_Privatekey_Length, (byte) 0);
		Util.arrayFillNonAtomic(publicKey, (short)(index * 65), ECDSA.EC_PublicKey_Length, (byte) 0);
	}
	
	/**
	 * 
	 */
	public void GenerateAccount() {
		byte[] path = {(byte)0x80, 0x00, 0x00, 0x2C, 
				(byte)0x80, 0x00, 0x00, 0x3C, 
				(byte)0x80, 0x00, 0x00, 0x00, 
					0x00, 0x00, 0x00, 0x00, 
					0x00, 0x00, 0x00, 0x00};
		for(byte i=0; i< 1; i++) {
			path[19] = i;			
			DeriveFromPath(path, (short) 0, privateKey, (short) (i*32), childChainCache, (short) 0);
			ChildPubKey(privateKey, (short) (i * 32), publicKey, (short)(i*65));
		}
		
	}
	
	/**
	 * 
	 * @param pubOut
	 * @param pubOutOff
	 * @param number
	 */
	public void GetAccount(byte[] pubOut, short pubOutOff, byte number) {
		Util.arrayCopyNonAtomic(publicKey, (short)(number*65), pubOut, pubOutOff, (short) 65);
	}
	
	/**
	 * 
	 * @param number
	 */
	public void SelecteAccount(byte number) {
		
		//		Derive Private Child Key and Child Chain Code From Path
		Util.arrayCopyNonAtomic(privateKey, (short) (number*32), childPriCache, (short)0, (short) 32);
		accountSelected = true;
	}
	
	/**
	 * 
	 * @param seedIn
	 * @param offset
	 * @return
	 */
	public short CompareSeed(byte[] seedIn, short offset) {
		return Util.arrayCompare(seedIn, offset, seed, (short)0, seed_length);
	}
	
	
	/**
	 * 
	 * @param buf
	 * @param bufOff
	 * @param bufLength
	 */
	public void ImportKeccak(byte[] buf, short bufOff, byte bufLength) {
		
		if(bufLength != (byte) ECDSA.Keccak256_Length) {
			ISOException.throwIt(ISO7816.SW_CORRECT_LENGTH_00);
		}
		Util.arrayCopyNonAtomic(buf, bufOff, hashCache, (short)0, bufLength);
		rlpReceived = true;
	}
	
	/**
	 * 
	 * @param signatureOut
	 * @param signatureOff
	 */
	public void Transaction(byte[] signatureOut, short signatureOff) {
		if(accountSelected == false || rlpReceived == false)
			ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		ecdsa.SignHash(hashCache, (short) 0, signatureOut, signatureOff, childPriCache, (short) 0);
		accountSelected = false;
		rlpReceived = false;
	}

	/**
	 * Get Public Key for Genuine Checking by ECDSA signature
	 * @param dataOut: public key buffer 
	 * @param dataOff: public key Offset
	 */
	public void GetPublicKeyGenuine(byte[] dataOut, short dataOff) {
		ecdsa.getGenuinePublicKey(dataOut, dataOff);
	}
	
	
	/**
	 * Sign ECDSA for Genuine Checking
	 * @param dataIn: input data  
	 * @param dataOff: offset input data
	 * @param dataLength: length input data
	 * @param signOut:  signature output
	 * @param signOff: offset signature output
	 */
	public void GenuineChecking(byte[] dataIn, short dataOff, short dataLength , byte[] signOut, short signOff) {
		ecdsa.signGenuine(dataIn, dataOff, dataLength, signOut, signOff); // signature ECDSA 
	}
}
