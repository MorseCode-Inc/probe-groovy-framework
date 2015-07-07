
package inc.morsecode.probes.groovy_framework;

import com.nimsoft.nimbus.NimAlarm;
import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimLog;
import com.nimsoft.nimbus.ci.ConfigurationItem;

import inc.morsecode.NDS;

public abstract class GroovyPlugin {
	
	protected NDS config;
	protected ConfigurationItem ci;
	protected NimLog log;

	protected Probe probe;
	
	public GroovyPlugin() {
		this.log= NimLog.getLogger(this.getClass());
	}
	
	public final void init(Probe probe, NDS config) {
		this.probe= probe;
		this.config= config;
		load(config, log);
	}
	
	/** 
	 * to be overridden by plugin
	 * @param config
	 */
	public void load(NDS config, NimLog log) {
	}
	
	public void execute(Probe probe, NimLog log) {
		
	}
	
	
	public void shutdown(Probe probe, NimLog log) {
		
	}
	
	public void setCi(ConfigurationItem ci) {
		this.ci = ci;
	}
	
	public ConfigurationItem getCi() {
		return ci;
	}
	
	public void alarm(int severity, String message, String key) {
		NimLog log= getLogger();
		// keep severity in valid ranges
		severity= Math.max(severity, 0);
		severity= Math.min(severity, 5);
		
		NimAlarm alarm= null;
		
		if (ci != null) {
			try {
				alarm= new NimAlarm(severity, message, getSubsystem(), key, getSource(), ci, (String)null);
			} catch (NimException error) {
				log.error("Failed to create Alarm: "+ severity +":"+ message +" ["+ error.getMessage() +" @"+ error.getStackTrace()[0].getFileName() +":"+ error.getStackTrace()[0].getLineNumber() +"]");
			}
			
		} else {
			
			alarm= new NimAlarm(severity, message, getSubsystem(), key, getSource());
			
		}
		
		try {
			if (alarm != null) {
				alarm.send();
				log.info("ALARM "+ alarm.getSeverity() +": "+ alarm.getMessage());
			}
		} catch (NimException error) {
			log.error("Failed to send Alarm: "+ severity +": "+ message +" ["+ error.getMessage() +" @"+ error.getStackTrace()[0].getFileName() +":"+ error.getStackTrace()[0].getLineNumber() +"]");
		} finally {
			if (alarm != null) { 
				alarm.close();
			}
		}
		
	}
	
	public String getSource() {
		return probe.getSource();
	}
	
	public String getSubsystem() {
		return probe.getSubsystemId();
	}
	
	public NimLog getLogger() {
		return log;
	}
	
	
}
