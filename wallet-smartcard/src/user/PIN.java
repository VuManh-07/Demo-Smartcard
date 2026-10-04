package user;

import javacard.framework.*;
import javacard.security.CryptoException;

public class PIN {
	private final byte 		TRY_LIMIT_PIN		= 0x05;
	private final byte 		MAX_SIZE_PIN		= 0x08;
	private final byte[] 	defautPin			= {0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31, 0x31};
//	private final byte[]	newPin				= new byte[8];
	
	private OwnerPIN UserPin = new OwnerPIN(TRY_LIMIT_PIN, MAX_SIZE_PIN);
	
	public short 				pinStatus			= 0;
	
	public static final short	pinNonActive	= 0;
	public static final short	pinActive		= 1;
	
	public void install(){
		UserPin.update(defautPin,(short)0,(byte)defautPin.length);
		return;
	}
	
	public void ResetAuthentication() {
		UserPin.reset();
	}
	
	public boolean UpdatePin(byte[] NewPin, short offsetNew, byte lengthNew, byte[] OldPin, short offsetOld, byte lengthOld){
		if(UserPin.check(OldPin, offsetOld, lengthOld) == true) {
			UserPin.update(NewPin, offsetNew, lengthNew);
			return true;			
		}
		else
			return false;
	}
	
	public boolean VerifyPin(byte[] buf, short offset, byte length){
		return UserPin.check(buf, offset, length);	
	}
	
	
	public boolean CheckAuthentication() {
		return UserPin.isValidated();
	}
	
	public void ResetPin() {
		try {
			UserPin.update(defautPin,(short) 0,(byte)defautPin.length);
		}
		catch(CryptoException ex){
			ISOException.throwIt(ex.getReason());
		}
	}
	
	public byte TryLimit() {
		return UserPin.getTriesRemaining();
	}
}
