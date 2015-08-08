package inc.morsecode.pagerduty;


import inc.morsecode.NDS;
import inc.morsecode.core.Encode;
import inc.morsecode.core.HttpGateway;
import inc.morsecode.core.MessageHandler;
import inc.morsecode.core.QueueSubscription;
import inc.morsecode.core.UIMMessage;
import inc.morsecode.nas.UIMAlarmNew;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDService;

import org.apache.tomcat.util.codec.binary.Base64;

import util.security.Crypto;
import util.security.codecs.SecurityCodec;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimRequest;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimSubscribe;
import com.nimsoft.nimbus.NimUserLogin;
import com.nimsoft.nimbus.PDS;

public class Probe extends HttpGateway implements MessageHandler {

	private QueueSubscription subscription;
	private boolean started= false;
	private int failures= 0;
	private boolean firstboot= true;
	
	
	public Probe(String[] args) throws NimException {
		super(args);
	}
	
	private boolean doOnce() {
		// try and connect to pager duty, see if we have our configuration setup correctly.
		
		NDS pd= config.seek("pagerduty");
		
		String apiKey= pd.get("auth/api_key", "missing configuration key pagerduty/auth/api_key");
		String subdomain= pd.get("auth/api_key", "missing configuration key pagerduty/auth/api_key");
		
		boolean isConfigured= pd.get("auth/confirmed", false);
		
		if (!isConfigured) { 
			log.info("Must configure probe with api credentials before it will function, refer to the Probe User Guide.");
			return false; 
		}
		
		
		
		return true;
		
	}
	
	
	public void probeCycle() {
		
		if (!started && firstboot) {
			firstboot= false;
			//do whatever we may need to, once!
			if (firstboot= !doOnce()) {
				
				return;
			}
		}
		
		
		// NimSubscribe subscription= new NimSubscribe("/UIM/MORSECODE");
		// subscription.subscribeForQueue(queuename, object, methodname);
		
		String clientName = "pagerduty_gtw";
		String queue = config.get("setup/subscription/queue", "pagerduty");
		int bulkSize = config.get("setup/subscription/bulk_size", 1);
		
		String address= config.get("setup/subscription/address", "/invalid/uim/address");
			
		if (subscription == null) {
			subscription= new QueueSubscription(address, clientName, queue, bulkSize);
		} 
		
		subscription.register(this);
		
		if (!subscription.isOk()) {
			try {
				subscription.subscribe();
				log.info("Subscription to queue "+ address +":"+ queue +" established.");
				started= true;
			} catch (NimException nx) {
				if (nx.getCode() == 1) { 
					
				} else if (nx.getCode() == 4) {
					log.error("Queue Subscription Failure "+ address +" queue="+ queue +" [Not Found]");
					log.error("Possible Causes:");
					log.error("\t1) Check that an attach queue exists on hub: "+ address +" named '"+ queue +"'");
					log.error("\t2) The configuration of this probe does not match your environment.");
				} else {
					log.error("Queue Subscription Failure "+ address +" queue="+ queue +" ["+ nx.getMessage() +"]");
					log.error("Possible Causes:");
					log.error("\t1) Reset the admin credentials for this probe using callback: set_admin");
					log.error("\t2) The configuration of this probe does not match your environment.");
				}
			}
		}
		
		if (!started) {
			
			
			if (failures > 3) {
				// something isnt configured correctly
				log.fatal("Aborting probe startup due to failures.  Check configurations for admin and queue subscription.");
				System.exit(1);
			}
			
			failures++;
			
			
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
  			probe.registerCallback(probe, "set_api", "set_api", new String[]{"subdomain", "api_key", "tld"});
  		

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

	
	@Override
	public boolean handle(UIMMessage message) {
		
		AlarmDatabase alarmManager= new AlarmDatabase();
		
		if ("alarm".equals(message.getSubject())) {
		} else if ("alarm_new".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmNew(message));
		} else if ("alarm_update".equals(message.getSubject())) {
		} else if ("alarm_assign".equals(message.getSubject())) {
		} else if ("alarm_close".equals(message.getSubject())) {
			
		}
		
		
		return true;
	}
	
	
	
	
	

	public void set_api(NimSession session, String subdomain, String api_token, String tld, PDS args) throws NimException {
		NDS response= new NDS();
		
		if (subdomain == null || "".equals(subdomain)) {
			response.set("status", "Error: subdomain cannot be empty or null.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
		if (api_token == null || "".equals(api_token)) {
			response.set("status", "Error: api_token cannot be empty or null.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
		if (tld == null || "".equals(tld)) {
			tld= "pagerduty.com";
			// response.set("status", "Error: top-level-domain (tld) cannot be empty or null.");
			// session.sendReply(0, response.toPDS());
			// return;
		}
		
		
		try {
			
			
			PDClient client= new PDClient(subdomain, tld, api_token);
			PagerDutyServicesAPI services= new PagerDutyServicesAPI(client);
			
			log.info("API Check <==> services.list [");
			for (PDService service : services.listServices(0, 25)) {
				log.info("\t"+ service.getId() +":"+ service.getName() +" "+ service.getServiceKey() +" ("+ service.getServiceUrl() +")");
			}
			log.info(" ]");
			
			
			
			
			// we logged in, need to write to the configuration file somehow now.
			NimRequest controller= new NimRequest("controller", "get_info");
			
			// System.out.println("/pagerduty/auth/api_key= '"+ Encode.encode(api_token) +"'");
			NDS status = writeConfig("/pagerduty/auth", "api_key", Encode.encode(api_token), controller);
			status = writeConfig("/pagerduty/auth", "subdomain", subdomain, controller);
			status = writeConfig("/pagerduty/auth", "tld", tld, controller);
			status = writeConfig("/pagerduty/auth", "confirmed", "yes", controller);
			
			status.setName("detail");
			
			System.out.println(status);
			
			response.add(status);
			
			response.set("status", "OK");
			
			controller.close();
		} catch (Throwable anything) {
			
			log.error("ERROR: "+ anything.toString());
			
		} finally {
			session.sendReply(0, response.toPDS());
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
			// return "hi8_#94/*%X+=dr[WUfOt\\y>\'IFn5?skSqz]JgYxN-,)2@(3wV<C1Rb\"{Dcup:L MGBZP6~aH;Em}.^QKjloA`Tv0$e|&7!";
			return "y>\'IFn5?shifOt\\kSqz]JgYxN-,)2@(3wV<Dcup:L MGBZP6~aH;Em8_#94/*%X+=dC1Rb\"{r[WU}.^QKjloA`Tv0$e|&7!";
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
