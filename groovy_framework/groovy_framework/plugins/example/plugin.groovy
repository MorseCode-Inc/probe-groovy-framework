import com.nimsoft.nimbus.NimLog;

import inc.morsecode.*;
import inc.morsecode.probes.groovy_framework.*;

/*
* 
*/
class Example extends GroovyPlugin {
	
	final static int CLEAR= 0;
	final static int INFO= 1;
	final static int WARN= 2;
	final static int MINOR= 3;
	final static int MAJOR= 4;
	final static int CRITICAL= 5;
	
	int n= 1;
	
	public Example() {
	}
	
	/**
	 * load()
	 * 
	 * Called when the probe boots and is restarted
	 * 
	 * Use this to get any information out of the configuration necessary 
	 * 
	 */
	public void load(NDS config, NimLog log) {
		
	}
	
	/**
	 * execute()
	 * 
	 * Called each cycle of the plugin as described in it's configuration (default = 5 minutes = 300 seconds)
	 * 
	 */
	public void execute(Probe probe, NimLog log) {
		
		if ((100 * Math.random()) % 2 == 0) {
			alarm(CLEAR, "Everything is OK!", "first_alarm");
			
		} else {
			alarm(MAJOR, "My first alarm from groovy!", "first_alarm");
		}
		
	}

	public String getSource() { return super.getSource(); }
	public String getSubsystem() { return super.getSubsystem(); }
	

}