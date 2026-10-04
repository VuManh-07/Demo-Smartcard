package user;

import javacard.security.MessageDigest;
import javacard.framework.Util;

public class HmacSHA512 {
	private static final short 			DIGEST_SIZE 	= 64;
	private static final byte 			IPAD 			= (byte)0x36;
	private static final byte 			OPAD 			= (byte)0x5c;
	private static final short 			BLOCK_SIZE 		= (short)128;
	private MessageDigest sha512;
	
	void install() {
		sha512 = MessageDigest.getInstance(MessageDigest.ALG_SHA_512, false);
	}
	
	public void hmac(byte[] key, short keyOffset, short keyLength, byte[] data, short dataOffset, short dataLength, byte[] out, short outOffset) {
		byte i;
		byte[] 	block 			= new byte[BLOCK_SIZE];
		short 	blockOffset		= (short) 0;
		for (i=0; i<2; i++) {
			Util.arrayFillNonAtomic(block, blockOffset, BLOCK_SIZE, (i == 0 ? IPAD : OPAD));
			for (short j=0; j<keyLength; j++) {
				block[(short)(blockOffset + j)] ^= key[(short)(keyOffset + j)];
			}
			sha512.update(block, blockOffset, BLOCK_SIZE);
			if (i == 0) {
				sha512.doFinal(data, dataOffset, dataLength, out, outOffset);
			}
			else {
				sha512.doFinal(out, outOffset, DIGEST_SIZE, out, outOffset);
			}			
		}
	}
}
