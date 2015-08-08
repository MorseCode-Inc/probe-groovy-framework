package inc.morsecode.core;

import org.apache.tomcat.util.codec.binary.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import util.security.Crypto;
import util.security.codecs.SecurityCodec;

public final class Decode {

	
	public static final String decode(String cypherText) {
		cypherText= new String(Base64.decodeBase64((cypherText).getBytes()));
		String phase1 = phase1(cypherText);
		String clear= Crypto.decode(phase1);
		return remove(new String(Base64.decodeBase64((clear).getBytes())));
	}


	private static String phase1(String cypherText) {
		return Crypto.decode(cypherText, new Secret());
	}
	
	
	private static final String remove(String salted) {
		return salted.substring(salted.indexOf(':') + 1);
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
