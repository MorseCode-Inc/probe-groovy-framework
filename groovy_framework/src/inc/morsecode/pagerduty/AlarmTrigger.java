package inc.morsecode.pagerduty;

import inc.morsecode.NDS;

public class AlarmTrigger {
	
	private NDS definition;
	
	public AlarmTrigger(NDS definition) {
		this.definition= definition;
	}

	public boolean isActive() {
		return definition.isActive();
	}
	
	public String getName() {
		return definition.getName();
	}
	
	public String getServiceKey(String ifNull) {
		return definition.get("service_key", ifNull);
	}
	
}
