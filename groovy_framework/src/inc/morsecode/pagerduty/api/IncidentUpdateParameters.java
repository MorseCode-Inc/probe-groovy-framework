package inc.morsecode.pagerduty.api;

import java.util.Map;

import util.json.JsonObject;
import inc.morsecode.NDS;

public class IncidentUpdateParameters extends NDS {

	public IncidentUpdateParameters() {
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(Map<String, Object> map) {
		super(map);
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(NDS nds) {
		super(nds);
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(String name, Map<String, Object> map) {
		super(name, map);
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(NDS nds, boolean reference) {
		super(nds, reference);
		// TODO Auto-generated constructor stub
	}

	public IncidentUpdateParameters(String name, JsonObject json) {
		super(name, json);
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return get("id");
	}
	
	public String getStatus() {
		return get("status");
	}
	
	public int getEscalationLevel() {
		return get("escalation_level", 1);
	}
}
