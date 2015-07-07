package inc.morsecode.probes.groovy_framework;


import inc.morsecode.NDS;
import inc.morsecode.NimLogPrintWriter;
import inc.morsecode.probes.http_gateway.Encode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.tomcat.util.codec.binary.Base64;

import util.security.Crypto;
import util.security.codecs.SecurityCodec;

import com.nimsoft.nimbus.NimConfig;
import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimLog;
import com.nimsoft.nimbus.NimProbe;
import com.nimsoft.nimbus.NimRequest;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimUserLogin;
import com.nimsoft.nimbus.PDS;

public class Probe extends NimProbe {
	
	public final static String PROBE_NAME= "groovy_framework";
	public final static String PROBE_VERSION= "1.01";
	public final static String PROBE_MANUFACTURER= "MorseCode Incorporated";
	
	public final static String SUBSYSTEM_ID= "3.6526.2";
	
	protected NimLog log;
	private static NDS config;
	private NDS activeProfiles;
	private HashMap<String, GroovyPlugin> plugins;
	
	private NDS controllerInfo= new NDS();
	
	
	public Probe(String[] args) throws NimException {
		this(PROBE_NAME, PROBE_VERSION, PROBE_MANUFACTURER, args);
	}

	private Probe(String name, String version, String manufacturer, String[] args) throws NimException {
		super(name, version, manufacturer, args);
		this.log= NimLog.getLogger(this.getClass());
		refreshConfiguration();
		NimLog.setLogLevel(getLogLevel());
		setStartPort(48005);
		
		NimLogPrintWriter error= new NimLogPrintWriter(log, NimLog.ERROR);
		NimLogPrintWriter out= new NimLogPrintWriter(log, NimLog.INFO);
		
		System.setErr(error);
		System.setOut(out);
	}

	
	public void refreshConfiguration() throws NimException {
		
		config= NDS.create(NimConfig.getInstance());
		
		if (plugins != null) {
			for (GroovyPlugin plugin : plugins.values()) {
				plugin.shutdown(this, log);
			}
		}
		
		activeProfiles= new NDS();
		plugins= new HashMap<String, GroovyPlugin>();
		for (NDS profile : config.seek("plugins")) {
			
			if (!profile.get("enabled", false)) { continue; }
			
			activeProfiles.add(profile);
			
			File file= new File("plugins/"+ profile.get("plugin") +"/plugin.groovy");
			
			if (!file.exists()) {
				//
				log.error(file +" does not exist for plugin profile: "+ profile.getName());
				continue;
			}
			
			try {
				GroovyPlugin plugin= (GroovyPlugin)GroovyUtils.getInstance(file);
				
				plugin.init(this, profile);
				plugins.put(profile.getName(), plugin);
				
			} catch (InstantiationException e) {
				log.error("Unable to load plugin: "+ file +" ["+ e.getMessage() +"]");
			} catch (IllegalAccessException e) {
				log.error("Unable to load plugin: "+ file +" ["+ e.getMessage() +"]");
			} catch (IOException e) {
				log.error("Unable to load plugin: "+ file +" ["+ e.getMessage() +"]");
			}
		}
	}
	
	
	
	public void execute() {
		
		
		for (NDS profile : activeProfiles) {
			
			File file= new File("plugins/"+ profile.get("plugin") +"/plugin.groovy");
			
			if (!file.exists()) {
				//
				log.error(file +" does not exist for plugin profile: "+ profile.getName());
				continue;
			}
			
			try {
				for (GroovyPlugin plugin : plugins.values()) {
							
					plugin.execute(this, log);
				}
				
			} catch (NullPointerException error) {
				
				log.error("Null Pointer in Plugin: "+ file +" at "+ error.getStackTrace()[0].getFileName() +":"+ error.getStackTrace()[0].getLineNumber() +"");
			} catch (Throwable error) {
				log.error("error running plugin: "+ file +" ["+ error.getMessage() +" @"+ error.getStackTrace()[0].getFileName() +":"+ error.getStackTrace()[0].getLineNumber() +"]");
			}
		}
	}
	
	public void shutdown() {
		
		for (GroovyPlugin plugin : plugins.values()) {
			plugin.shutdown(this, log);
		}
	}
	
	public void reload(NimSession session, PDS args) throws NimException {
		log.info("** RELOAD **");
		refreshConfiguration();
		session.sendReply(0, new PDS());
	}
	
	
	static public long getInterval() {
		long ifNull= config.get("setup/run.interval", 60);
		long cycle= config.get("setup/cycle", ifNull);
		return cycle * 1000;
	}
	
	/**
	 * 
	 * @return sample rate is the interval in seconds
	 */
	static public int getSampleRate() {
		return (int)(getInterval() / 1000);
	}
	
	static public int getLogLevel() {
		return config.get("setup/loglevel", NimLog.INFO);
	}
	
	public String getSubsystemId() {
		return config.get("setup/subsystem_id", config.get("setup/subsystem", SUBSYSTEM_ID));
	}
	
	/*
	public String getSource() {
		return null;
	}
	*/
	
	
	public static void main(String[] args) {
		
		try {
		
			// String name= args[0];
			// String version= args[1];
			// String manufacturer= args[2];
			
			Probe probe= new Probe(args);
			
			long interval= Probe.getInterval();
			probe.registerCallbackOnTimer(probe, "execute", interval, true);
			probe.registerCallback(probe, "reload", "reload");
		
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


	public NDS getQoSDefinitions() {
		return config.seek("QOS_DEFINITIONS", true);
	}

	public String getDefaultCIPath() {
		return "2.1.1";
	}
	
	public NimLog getLogger() {
		return log;
	}

	public String getSource() {
		return null;
	}
	
	public NDS updateControllerInfo() throws NimException {
		this.controllerInfo= getControllerInformation();
		return controllerInfo;
	}

	   
		private NDS call(String address, String command, String[] ... params) throws NimException {
			
			NDS args= new NDS();
			
			for (String[] arg: params) {
				if (arg.length == 2) {
					args.set(arg[0], arg[1]);
				}
			}
			
			NimRequest request= new NimRequest(address, command, args.toPDS());
			
			NDS response= NDS.create(request.send());
			
			request.close();
			return response;
		}
	    
		
		public NDS call(String address, String command, int retries, String[] ... params) throws NimException {
			while (retries-- >= 0) {
				try {
					return call(address, command, params);
				} catch (NimException nx) {
					if (retries <= 0) {
						throw nx;
					}
				}
			}
			return null;
		}
		
		
		private NDS getControllerInformation() throws NimException {
			
			NDS info= call("controller", "get_info", 1);
			
			return info;
			
		}
		
	public void set_admin(NimSession session, String user, String password, String confirm, PDS args) throws NimException {
		NDS response= new NDS();
		
		if (user == null || "".equals(user)) {
			response.set("status", "Error: user cannot be empty or null.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
		if (password == null || confirm == null || "".equals(password) || "".equals(confirm)) {
			response.set("status", "Error: password cannot be empty or null.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
		if (!password.equals(confirm)) {
			response.set("status", "Error: confirm does not match.");
			session.sendReply(0, response.toPDS());
			return;
		}
		
		try {
			NimUserLogin.login(user, password);
		} catch (NimException error) {
			response.set("status", "Error: "+ error.getMessage());
			session.sendReply(0, response.toPDS());
			return;
		}
		
		// we logged in, need to write to the configuration file somehow now.
		NimRequest controller= new NimRequest("controller", "get_info");
		
		NDS status = writeConfig("/setup/admin", "token", Encode.encode(user), controller);
		status = writeConfig("/setup/admin", "key", Encode.encode(password), controller);
		status = writeConfig("/setup/admin", "enabled", "yes", controller);
		status.setName("detail");
		
		System.out.println(status);
		
		response.add(status);
		
		response.set("status", "OK");
		response.set("token", Encode.encode(user));
		response.set("key", Encode.encode(password));
		
		controller.close();
		
		
		session.sendReply(0, response.toPDS());
	}


	private NDS writeConfig(String section, String key, String value, NimRequest controller) throws NimException {
		NDS nds= new NDS();
		
		nds.set("name", Probe.PROBE_NAME);		// probe name
		nds.set("section", section);		// 
		nds.set("key", key);				// 
		nds.set("value", value);	// 
		nds.set("lockid", 1);	// 
		nds.set("robot", "/"+ controllerInfo.get("domain") +"/"+ controllerInfo.get("hubname") +"/"+ controllerInfo.get("robotname"));	// 
		
		NDS status= NDS.create(controller.send("probe_config_set", nds.toPDS()));
		return status;
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
	
		/**
	 	*
	 	*/
		public String getAlphabet(String[] c) {
			return "y>\'ifOIFn5?8_#94/*%X+=dC1Rb\"sht\\kSqz]JgYxN-,)2@(3wV<Dcup:L MGBZP6~aH;Em{r[WU}.^QKjloA`Tv0$e|&7!";
		} /* getAlphabet */
	
		/**
	 	*
	 	*/
		public int getRotator(String[] c) {
			return 4413;
		} /* getRotator */
	
	
	}

	
	/*
	public AlarmMessageTemplate getMessage(String name) {
	}
	*/

}


}
