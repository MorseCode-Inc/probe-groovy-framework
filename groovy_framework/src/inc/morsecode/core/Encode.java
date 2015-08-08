package inc.morsecode.core;

import org.apache.tomcat.util.codec.binary.Base64;

import util.security.Crypto;
import util.security.codecs.SecurityCodec;

public final class Encode {

	/**
	 * @param args
	 */
	
	public static final String encode(String text) {
		text= add(text);
		text= new String(Base64.encodeBase64((text).getBytes()));
		String phase2 = phase2(text);
		
		return new String(Base64.encodeBase64((Crypto.encode(phase2, new Secret()).getBytes())));
	}


	private static String phase2(String text) {
		return Crypto.encode(text);
	}
	
	private static final String add(String text) {
		return "salt:"+ text;
	}
	
	/**
 	*
 	*
 	*/
	private static class Secret implements SecurityCodec {
	
		/**
	 	*
	 	*/
		public String getAlphabet(String[] c) {
			return "hi8_#94/*%X+=dr[WUfOt\\y>\'IFn5?skSqz]JgYxN-,)2@(3wV<C1Rb\"{Dcup:L MGBZP6~aH;Em}.^QKjloA`Tv0$e|&7!";
		} /* getAlphabet */
	
		/**
	 	*
	 	*/
		public int getRotator(String[] c) {
			return 7331;
		} /* getRotator */
	
	
	}


}
