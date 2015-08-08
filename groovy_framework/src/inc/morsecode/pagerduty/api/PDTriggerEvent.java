package inc.morsecode.pagerduty.api;

import java.util.Map;

import util.json.JsonObject;

import inc.morsecode.NDS;

public class PDTriggerEvent {
	
	private PDService service;

	public PDTriggerEvent() {
	}

	public PDTriggerEvent(PDService service) {
		this.service= service;
	}
	
	public PDService getService() {
		return service;
	}
	
	public JsonObject toJson() {
		JsonObject json= service.toJson();
		
		return json;
	}

}
