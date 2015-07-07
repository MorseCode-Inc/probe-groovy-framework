package inc.morsecode.probes.groovy_framework;


import inc.morsecode.NDS;
import inc.morsecode.etc.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nimsoft.nimbus.NimConfig;
import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimLog;
import com.nimsoft.nimbus.NimProbe;
import com.nimsoft.nimbus.NimQoS;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimSessionListener;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.ci.ConfigurationItem;
import com.nimsoft.nimbus.ci.Device;

public class Probe extends NimProbe {
	
	public final static String PROBE_NAME= "groovy_framework";
	public final static String PROBE_VERSION= "1.01";
	public final static String PROBE_MANUFACTURER= "MorseCode Incorporated";
	
	public final static String SUBSYSTEM_ID= "3.6526.2";
	
	protected NimLog log;
	private static NDS config;
	private NDS activeProfiles;
	private HashMap<String, GroovyPlugin> plugins;
	
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
	
	
}
