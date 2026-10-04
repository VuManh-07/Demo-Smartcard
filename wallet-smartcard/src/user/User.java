package user;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import org.globalplatform.*;


public class User extends Applet {
	
	// INS Command	
	final byte INS_GENERATE_SEED 					= 0x00;
	final byte INS_IMPORT_SEED						= 0x01;

	final byte INS_TRANSACTION						= 0x06;
	final byte INS_STATUS_CARD						= 0x0F;

	final byte INS_VERIFY_PIN 						= 0x07;
	final byte INS_CHANGE_PIN 						= 0x08;

	final byte INS_IMPORT_INFO						= 0x0D;
	final byte INS_GET_INFO							= 0x0E;
	
	final byte INS_IMPORT_ACCOUNT					= 0x16;
	final byte INS_GET_ACCOUNT	 					= 0x03;	
	final byte INS_DELETE_ACCOUNT					= 0x18;
	
	final byte INS_RESET_WALLET						= 0x02;
	final byte INS_RESET_WALLET_AUTH				= 0x23;
	
	private static final byte INS_GP_INITIALIZE_UPDATE      = (byte) 0x50;	
	private static final byte INS_GP_EXTERNAL_AUTHENTICATE   = (byte) 0x82;
	
	// Objects 
	private	PIN 		pin					= new PIN(); 
	private HDWallet	wallet				= new HDWallet();
	private Infomation	infomation			= new Infomation();
	private SecureChannel secureChannel;
	
	
	// Parameters
	private short length;
	private boolean isAuthenticated;
	private byte INS;
	
	
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new User().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}	
	
	private User() {	
		pin.install();	
		wallet.install();
	}

	
	public void process(APDU apdu) {
		if (selectingApplet()) {
			pin.ResetAuthentication();
			if (secureChannel != null) secureChannel.resetSecurity();    
			return;
		}
		byte[] buffer = apdu.getBuffer();
		INS = buffer[ISO7816.OFFSET_INS];
		
	      // Get a reference to the GlobalPlatform SecureChannel
		if (secureChannel == null) secureChannel = GPSystem.getSecureChannel();   
      
		length = apdu.setIncomingAndReceive();

		isAuthenticated = false;
		final byte SC_MASK = SecureChannel.AUTHENTICATED | SecureChannel.C_DECRYPTION | SecureChannel.C_MAC | SecureChannel.R_MAC | SecureChannel.R_ENCRYPTION;
      
		if ( (secureChannel.getSecurityLevel() & SC_MASK) == SC_MASK) {
	         // Unwrap, including the entire APDU which is required for verifying the MAC
	        secureChannel.unwrap(buffer, (short)0, (short)(ISO7816.OFFSET_CDATA + length));                  
	         // Now flag the command as being authenticated
	        isAuthenticated = true; 
		}		
				
		
		
		/*
		 * 		Check Secure Command
		 */
		if(INS != INS_GP_INITIALIZE_UPDATE && INS != INS_GP_EXTERNAL_AUTHENTICATE) {
			if (!isAuthenticated) 
				ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);	
		}
		
		
		
		/*
		 *		Check Non Installed Status of Wallet
		 */		
		if(INS == INS_IMPORT_INFO) {
			if(infomation.WalletStatus != Infomation.WalletNonInstalled)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_CONDITIONS_NOT_SATISFIED);	
		}		
		
		
		
		/*
		 * 		Check Installed Status of Wallet
		 */		
		if(INS == INS_GENERATE_SEED || INS == INS_IMPORT_SEED) {
			if(infomation.WalletStatus != Infomation.WalletInstalled)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
		
		
		
		/*
		 * 		Check Active Status of Wallet
		 */		
		if(	INS == INS_VERIFY_PIN || INS == INS_TRANSACTION || INS == INS_RESET_WALLET) {
			if(infomation.WalletStatus != Infomation.WalletActived)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
		
		
		
		/*
		 * 		Check Authenticate Personal by PIN
		 */		
		if(		INS == INS_CHANGE_PIN ||
				INS == INS_GET_ACCOUNT||
				INS == INS_IMPORT_ACCOUNT||
				INS == INS_DELETE_ACCOUNT||
				INS == INS_RESET_WALLET_AUTH
				) {
			if(pin.CheckAuthentication() == false)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_CONDITIONS_NOT_SATISFIED);
		}
		
		switch (INS) {
		case INS_GP_INITIALIZE_UPDATE:
	         secureChannel.resetSecurity();
	         length = secureChannel.processSecurity(apdu);
	         apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, length);
	         break;
	         
	      case INS_GP_EXTERNAL_AUTHENTICATE:
	         length = secureChannel.processSecurity(apdu);
	         apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, length);
	         break;
	          
		
		/*
		 * 	Generate Seed Root Function
		 */
		case INS_GENERATE_SEED:	
			length = wallet.SeedRootGenerate(buffer, (short) 0);
			processOut(apdu, buffer, (short) 0,  length, buffer, (short)0, ISO7816.SW_NO_ERROR);
			break;
			
		/*
		 * 	Import Seed Root Function
		 */
		case INS_IMPORT_SEED:
			if(buffer[ISO7816.OFFSET_LC] % 16 != 0)
				processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_CORRECT_LENGTH_00);
			wallet.ImportSeedRoot(buffer, (short) ISO7816.OFFSET_CDATA, buffer[ISO7816.OFFSET_LC]);
			processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_NO_ERROR);
			infomation.WalletStatus = Infomation.WalletActived;
			break;
			
		/*
		 * 	Get Account Function 
		 */
		case INS_GET_ACCOUNT:
			wallet.GetAccount(buffer, (short) 0, buffer[ISO7816.OFFSET_P1]);	
			processOut(apdu, buffer, (short) 0, (short) ECDSA.EC_PublicKey_Length, buffer, (short) 0, ISO7816.SW_NO_ERROR);
			break;	
		
		/*
		 * Import Other Account
		 */
		case INS_IMPORT_ACCOUNT:
			if( wallet.ImportAccount(buffer, (short) ISO7816.OFFSET_CDATA, (short) buffer[ISO7816.OFFSET_LC], buffer[ISO7816.OFFSET_P1]) == true)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_NO_ERROR);
			else
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_DATA_INVALID);
			break;
		
		/*
		 * Delete Other Account
		 */
		case INS_DELETE_ACCOUNT:
			wallet.DeleteAccount(buffer[ISO7816.OFFSET_P1]);
			processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_NO_ERROR);
			break;
			
		/*
		 * 	Reset wallet non Authentication
		 */
		case INS_RESET_WALLET:	
			if(infomation.WalletStatus == Infomation.WalletNonInstalled)
				processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_COMMAND_NOT_ALLOWED);
			if( wallet.CompareSeed(buffer, ISO7816.OFFSET_CDATA)  != 0)
				processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_DATA_INVALID);
			infomation.WalletStatus = Infomation.WalletInstalled;
			pin.ResetPin();
			wallet.Reset();
			processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_NO_ERROR);
			break;
		
		/*
		 * Reset wallet with Authentication
		 */
		case INS_RESET_WALLET_AUTH:
			if(pin.VerifyPin(buffer, ISO7816.OFFSET_CDATA, buffer[ISO7816.OFFSET_LC]) == false)
				processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_DATA_INVALID);
			infomation.WalletStatus = Infomation.WalletInstalled;
			pin.ResetPin();
			wallet.Reset();
			processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_NO_ERROR);
			break;
			
			
		/*
		 * Sign Transaction
		 */	
		case INS_TRANSACTION:
			if(pin.VerifyPin(buffer, (short)(ISO7816.OFFSET_CDATA + 1), (byte)8) == false)
				processOut(apdu, buffer, (short) 0, (byte) 0, buffer, (short) 0, ISO7816.SW_DATA_INVALID);
			wallet.SelecteAccount(buffer[ISO7816.OFFSET_CDATA]);
			wallet.ImportKeccak(buffer, (short)(ISO7816.OFFSET_CDATA + 9), (byte)32);
			wallet.Transaction(buffer, (short) 0);
			processOut(apdu, buffer, (short) 0,  (short)ECDSA.ECDSA_Signature_Length, buffer, (short)0, ISO7816.SW_NO_ERROR);
			break;
		
		/*
		 * 	Verify pin
		 */
		case INS_VERIFY_PIN:
			if(pin.pinStatus != PIN.pinActive) {
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_COMMAND_NOT_ALLOWED);
			}
			if( pin.VerifyPin(buffer, ISO7816.OFFSET_CDATA, buffer[ISO7816.OFFSET_LC]) == true)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_NO_ERROR);
			else
				buffer[0] = pin.TryLimit();
				processOut(apdu, buffer, (short) 0,  (short) 1, buffer, (short)0, ISO7816.SW_DATA_INVALID);
			break;
		
		/*
		 * 	Change PIN
		 */
		case INS_CHANGE_PIN:		
			if( pin.UpdatePin(buffer, (short) ISO7816.OFFSET_CDATA, (byte)8, buffer, (short)(ISO7816.OFFSET_CDATA + 8), (byte)8) == true)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_NO_ERROR);
			else
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_DATA_INVALID);
			break;
		
		/*
		 * Get Status Wallet
		 */
		case INS_STATUS_CARD:
			processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, infomation.WalletStatus);
			break;
			
		/*
		 * Import Information Wallet
		 */
		case INS_IMPORT_INFO:
			if(infomation.WalletStatus == Infomation.WalletActived)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_COMMAND_NOT_ALLOWED);
			infomation.ImportInfomation(buffer, ISO7816.OFFSET_CDATA, buffer[ISO7816.OFFSET_LC]);
			processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_NO_ERROR);
			break;
			
		/*
		 * Get Information Wallet
		 */
		case INS_GET_INFO:
			if(infomation.WalletStatus == Infomation.WalletNonInstalled)
				processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_COMMAND_NOT_ALLOWED);	
			length = infomation.GetInfomation(buffer, (short)0);
			processOut(apdu, buffer, (short) 0,  length, buffer, (short)0, ISO7816.SW_NO_ERROR);
			break;
	
		default:
			processOut(apdu, buffer, (short) 0,  (short) 0, buffer, (short)0, ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
		
	public void deselect() {      
	      if (secureChannel != null) secureChannel.resetSecurity();      
	   }
	   
    public void processOut(APDU apdu, byte[] data, short dataOffset, short dataLength, byte[] buf, short bufOff, short sw) {
    	Util.arrayCopy(data, dataOffset, buf, bufOff, (short)dataLength); 
	 	Util.setShort(buf, dataLength, sw);		
	 	dataLength += 2;
	 	short len =  secureChannel.wrap(buf, bufOff, dataLength);
	 	apdu.setOutgoing();
		apdu.setOutgoingLength(len);
		apdu.sendBytesLong(buf, bufOff, len);
		ISOException.throwIt(sw);
    }

}
