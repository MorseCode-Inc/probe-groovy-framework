package inc.morsecode.pagerduty;


import java.io.IOException;
import java.util.Collection;
import java.util.List;

import inc.morsecode.NDS;
import inc.morsecode.core.Encode;
import inc.morsecode.core.HttpGateway;
import inc.morsecode.core.MessageHandler;
import inc.morsecode.core.QueueSubscription;
import inc.morsecode.core.UIMMessage;
import inc.morsecode.nas.UIMAlarmNew;
import inc.morsecode.nas.UIMAlarmUpdate;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDService;

import org.apache.tomcat.util.codec.binary.Base64;

import util.json.ex.MalformedJsonException;
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
	
	public final static String PROBE_NAME= "pagerduty_gtw";
	public final static String PROBE_VERSION= "0.12";
	public final static String QOS_GROUP= "PagerDuty";
	
	private boolean idle= true;
	
	private PDClient client;
	
	public Probe(String[] args) throws NimException {
		super(PROBE_NAME, PROBE_VERSION, args);
	}
	
	private boolean doOnce() {
		// try and connect to pager duty, see if we have our configuration setup correctly.
		
		NDS pd= config.seek("pagerduty", true);
		
		// log.info(config.toString());
		
		String apiKey= pd.get("auth/api_key", "missing configuration key pagerduty/auth/api_key");
		String rawApiKey= pd.get("auth/raw_api_key", null);
		String subdomain= pd.get("auth/subdomain", "missing configuration key pagerduty/auth/api_key");
		
		if ("missing configuration key pagerduty/auth/api_key".equals(subdomain)) {
			// missing subdomain
			throw new RuntimeException("Missing required auth/subdomain key in configuration under <pagerduty> section.");
		}
		
		apiKey= Decode.decode(apiKey);
		
		
		boolean isConfigured= pd.get("auth/confirmed", false);
		
		if (!isConfigured) { 
			log.info("Must configure probe with api credentials before it will function, refer to the Probe User Guide.");
			return false; 
		}
		
		this.client = new PDClient(subdomain, apiKey);
		
		// System.out.println("decoded api key = "+ apiKey);
		
		try {
			NimRequest controllerRequest = controllerRequest();
			List<PDService> services= client.incidents().listServices();
			// System.out.println(services);
			// write the services that were discovered back to the configuration file
			
			this.writeConfig("pagerduty", "services", (List)services, controllerRequest);
			
			
		} catch (NimException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedJsonException e) {
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	public PDClient getClient() {
		return client;
	}


	public void probeCycle() {

		
		if (!started && firstboot) {
			started= true;
			firstboot= false;
			//do whatever we may need to, once!
			if (firstboot= !doOnce()) {
				return;
			}
		}
		
		
		if (!isAdmin()) {
			log.info("UIM admin user rights are required to subscribe to the message bus.  Use the set_admin callback to configure this probe.");
		} else {
			idle= false;
		}
		
		if (idle) { 
			failures= 1 + (failures % 50);
			if (failures < 2) {
				log.warn("Probe is in idle mode... waiting for administrator. Restart the probe and review log messages.");
			}
			return; 
		}
		
		
		boolean ok= checkSubscription();
		
		if (!ok) {
			if (failures > 3) {
				// something isnt configured correctly
				log.fatal("Aborting due to failures.  Check configurations for admin and queue subscription.");
				System.exit(1);
			}
			
			failures++;
		}
		
		
		
	}

	public boolean checkSubscription() {
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
				return subscription.isOk();
			} catch (NimException nx) {
				if (nx.getCode() == 1) { 
					
				} else if (nx.getCode() == 4) {
					log.error("Queue Subscription Failure "+ address +" queue="+ queue +" [Not Found]");
					log.error("Possible Causes:");
					log.error("\t- Check that an attach queue exists on hub: "+ address +" named '"+ queue +"'");
					log.error("\t- The configuration of this probe does not match your environment.");
				} else {
					log.error("Queue Subscription Failure "+ address +" queue="+ queue +" ["+ nx.getMessage() +"]");
					log.error("Possible Causes:");
					log.error("\t- Reset the admin credentials for this probe using callback: set_admin");
					log.error("\t- The configuration of this probe does not match your environment.");
				}
				
				
			}
			
			return false;
		}
		
		return true;
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
		
		AlarmManager alarmManager= new AlarmManager(client);
		
		if ("alarm".equals(message.getSubject())) {
		} else if ("alarm_new".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmNew(message));
		} else if ("alarm_update".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmUpdate(message));
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
			return "hi8_#94/*%X+=dr[WUfOt\\y>\'IFn5?skSqz]JgYxN-,)2@(3wV<C1Rb\"{Dcup:L MGBZP6~aH;Em}.^QKjloA`Tv0$e|&7!";
			// return "y>\'IFn5?shifOt\\kSqz]JgYxN-,)2@(3wV<Dcup:L MGBZP6~aH;Em8_#94/*%X+=dC1Rb\"{r[WU}.^QKjloA`Tv0$e|&7!";
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
