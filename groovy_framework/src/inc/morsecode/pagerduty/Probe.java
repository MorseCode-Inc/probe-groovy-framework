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
import inc.morsecode.nas.UIMAlarmAssign;
import inc.morsecode.nas.UIMAlarmClose;
import inc.morsecode.nas.UIMAlarmNew;
import inc.morsecode.nas.UIMAlarmUnassign;
import inc.morsecode.nas.UIMAlarmUpdate;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDServiceUrls;
import inc.morsecode.pagerduty.api.PagerDutyServicesAPI;
import inc.morsecode.pagerduty.api.PagerDutyUsersAPI;
import inc.morsecode.pagerduty.data.PDService;
import inc.morsecode.pagerduty.data.PDTriggerEvent;
import inc.morsecode.pagerduty.data.PDUser;

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
	private AssignmentTriggers assignmentTriggers;
	private AlarmTriggersConfig alarmTriggers;
	
	public final static String PROBE_NAME= "pagerdutygtw";
	public final static String PROBE_VERSION= "0.12";
	public final static String QOS_GROUP= "PagerDuty";
	
	private boolean idle= true;
	
	private PDClient client;
	private ServiceMapper smap;
	private PDServiceUrls urls;
	
	public Probe(String[] args) throws NimException {
		super(PROBE_NAME, PROBE_VERSION, args);
	}
	
	private boolean doOnce() {
		// try and connect to pager duty, see if we have our configuration setup correctly.
		
		NDS pd= config.seek("pagerduty", true);
		
		// log.info(config.toString());
		
		assignmentTriggers= new AssignmentTriggers(pd.seek("assignment_triggers", true));
		alarmTriggers= new AlarmTriggersConfig(pd.seek("alarm_triggers", true));
		
	
		String apiKey= pd.get("auth/api_key", "missing configuration key pagerduty/auth/api_key");
		String rawApiKey= pd.get("auth/_api_key", null);
		String subdomain= pd.get("auth/subdomain", "missing configuration key pagerduty/auth/subdomain");
		
		if ("missing configuration key pagerduty/auth/api_key".equals(apiKey) && rawApiKey == null || "".equals(rawApiKey)) {
			// missing subdomain
			throw new RuntimeException("Missing required auth/api_key key in configuration under <pagerduty> section.");
		}
		
		if ("missing configuration key pagerduty/auth/subdomain".equals(subdomain)) {
			// missing subdomain
			throw new RuntimeException("Missing required auth/subdomain key in configuration under <pagerduty> section.");
		}
		
		apiKey= Decode.decode(apiKey);
		
		urls= new PDServiceUrls(pd.seek("service_urls"));
		
		
		boolean isConfigured= pd.get("auth/confirmed", false);
		
		if (!isConfigured) { 
			log.info("Must configure probe with api credentials before it will function, refer to the Probe User Guide.");
			return false; 
		}
		
		String userid= pd.get("auth/userid");
		
		if (userid == null) {
			
		}
		this.client = new PDClient(subdomain, apiKey, urls, userid);
		
		// System.out.println("decoded api key = "+ apiKey);
		
		try {
			NimRequest controllerRequest = controllerRequest();
			List<PDService> services= client.services().listServices();
			// System.out.println(services);
			// write the services that were discovered back to the configuration file
			
			this.writeConfig("pagerduty", "services", (List)services, controllerRequest);
			
			smap= new ServiceMapper(services, config.seek("pagerduty/mapping/services", true));
			
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
		
		String clientName = getProbeName();
		String queue = config.get("setup/subscription/queue", "pagerduty");
		int bulkSize = config.get("setup/subscription/bulk_size", 1, 1, 100);
		
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
					log.error("\t Check that an attach queue exists on hub: "+ address +" named '"+ queue +"'");
					log.error("\t The configuration of this probe does not match your environment.");
				} else {
					log.error("Queue Subscription Failure "+ address +" queue="+ queue +" ["+ nx.getMessage() +"]");
					log.error("Possible Causes:");
					log.error("\t Reset the admin credentials for this probe using callback: set_admin");
					log.error("\t The configuration of this probe does not match your environment.");
				}
				
				
			}
			
			return false;
		}
		
		return true;
	}
	
	@Override
	public void shutdown() {
		subscription.stop();
		super.shutdown();
		
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
  			probe.registerCallback(probe, "set_api", "set_api", new String[]{"userid", "subdomain", "api_key", "tld"});
  			
  			
  		

  			probe.registerCallbackOnTimer(probe, "execute", getInterval(), true);
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
		
		AlarmManager alarmManager= new AlarmManager(alarmTriggers, assignmentTriggers, client, smap);
		
		
		// FIRST: Decide if this message is even something we care about.
		// if not, then return as fast as we can.
		
		if ("alarm".equals(message.getSubject())) {
			// raw alarm messages from probes, not always the best source to use
			return true;
			
		} else if ("alarm_new".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmNew(message));
			
		} else if ("alarm_update".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmUpdate(message));
			
		} else if ("alarm_unassign".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmUnassign(message));
			
		} else if ("alarm_assign".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmAssign(message));
			
		} else if ("alarm_close".equals(message.getSubject())) {
			alarmManager.handle(new UIMAlarmClose(message));
			
		}
		
		
		return true;
	}
	
	
	
	
	

	public void set_api(NimSession session, String userid, String subdomain, String api_token, String tld, PDS args) throws NimException {
		NDS response= new NDS();
		
		if (userid == null || "".equals(userid)) {
			response.set("status", "Error: userid cannot be empty or null.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
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
			
			
			PDClient client= new PDClient(subdomain, tld, api_token, urls, userid);
			
			PagerDutyServicesAPI servicesAPI= new PagerDutyServicesAPI(client);
			PagerDutyUsersAPI usersAPI= new PagerDutyUsersAPI(client);
			
			log.info("API Check <==> services.list [");
			for (PDService service : servicesAPI.listServices(0, 25)) {
				log.info("\t"+ service.getId() +":"+ service.getName() +" "+ service.getServiceKey() +" ("+ service.getServiceUrl() +")");
			}
			log.info(" ]");
			
			
			try {
				PDUser user= usersAPI.getUser(userid);
				log.info("User Information: \n"+ user.toJson());
				
				// we logged in, need to write to the configuration file somehow now.
				NimRequest controller= new NimRequest("controller", "get_info");
				
				// System.out.println("/pagerduty/auth/api_key= '"+ Encode.encode(api_token) +"'");
				NDS status = writeConfig("/pagerduty/auth", "api_key", Encode.encode(api_token), controller);
				status = writeConfig("/pagerduty/auth", "subdomain", subdomain, controller);
				status = writeConfig("/pagerduty/auth", "userid", userid, controller);
				status = writeConfig("/pagerduty/auth", "tld", tld, controller);
				status = writeConfig("/pagerduty/auth", "confirmed", "yes", controller);
				
				status.setName("detail");
				
				System.out.println(status);
				
				response.add(status);
				
				response.set("status", "OK");
				
				controller.close();
				
				writeCache(getProbeName() +".pduser", user, true);
				
			} catch (RuntimeException x) {
				response.set("status", "ERR");
				response.set("message", "Invalid User Information: "+ userid);
				response.set("reason", x.getMessage());
				
				log.error("Invalid User Information: "+ userid +" ["+ x.getMessage() +"]");
				
			}
			

		} catch (Throwable anything) {
			response.set("status", "ERR");
			response.set("message", "Fatal Error");
			response.set("reason", anything.getMessage());
			
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
