package inc.morsecode.pagerduty;

import inc.morsecode.NDS;

public class AssignmentTrigger {
	
	private NDS definition;
	
	public AssignmentTrigger(NDS definition) {
		this.definition= definition;
	}

	public boolean isActive() {
		return definition.isActive();
	}
	
	public String getName() {
		return definition.getName();
	}
	
	public String getUimUsername(String ifNull) {
		return definition.get("uim_user", ifNull);
	}
	
	public String getServiceKey(String ifNull) {
		return definition.get("service_key", ifNull);
	}
	
}
