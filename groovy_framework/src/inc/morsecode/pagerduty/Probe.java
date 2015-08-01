package inc.morsecode.pagerduty;


import org.apache.tomcat.util.codec.binary.Base64;

import util.security.Crypto;
import util.security.codecs.SecurityCodec;

import com.nimsoft.nimbus.NimException;

public class Probe extends HttpGateway {

	private QueueSubscription subscription;
	
	public Probe(String[] args) throws NimException {
		super(args);
	}
	
	
	public void probeCycle() {
		
		if (subscription == null || !subscription.isOk()) {
			subscription= new QueueSubscription("pagerduty_gtw", config.get("", "pagerduty"), config.get("bulk_size", 1));
		}
		
		try {
			subscription.subscribe();
		} catch (NimException nx) {
			log.error("Queue Subscription Failure: "+ nx.getMessage());
		}
	}
	
	public static void main(String[] args) {

  		try {
  		
  			// String name= args[0];
  			// String version= args[1];
  			// String manufacturer= args[2];
  			
  			Probe probe= new Probe(args);
  			
  			System.out.println("Loading...");
  			probe.registerCallback(probe, "reload", "reload");
  			probe.registerCallback(probe, "set_admin", "set_admin", new String[]{"username", "password", "confirm"});
  		

  			probe.registerCallbackOnTimer(probe, "execute", 3000, true);
			// probe.registerCallbackOnTimer(probe, "execute", getInterval(), true);
  			do {
  				
  				if (probe.isStoppingOrRestarting()) {
  					probe.refreshConfiguration();
  				}
  				
  			} while (probe.doForever());
  			
  			probe.shutdown();
  		
  		} catch (Throwable error) {
  			error.printStackTrace();
  		}

	}

	

private final static class Decode {

	
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

}
