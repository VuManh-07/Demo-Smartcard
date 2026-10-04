package user;

import javacard.framework.Util;

public class Infomation {
	
	public byte[] 						WalletInformation	= new byte[250];	
	public short						WalletLength;
	public short						WalletStatus 		= 0;
	
	public static final short 			WalletActived 	 	= 2;
	public static final short 			WalletInstalled 	= 1;
	public static final short 			WalletNonInstalled 	= 0;
	
	
	public void ImportInfomation(byte[] dataIn, short dataOff, byte dataLength) {
		WalletLength = (short) dataLength;
		Util.arrayCopyNonAtomic(dataIn, dataOff, WalletInformation, (short) 0 , dataLength);
		WalletStatus = WalletInstalled;
	}
	
	public short GetInfomation(byte[] dataOut, short dataOff) {
		Util.arrayCopyNonAtomic(WalletInformation, (short)(0), dataOut, dataOff, WalletLength);
		return WalletLength;
	}
	
}
