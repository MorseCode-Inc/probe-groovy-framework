package inc.morsecode.pagerduty;

import java.util.Map;

import util.json.JsonObject;

import inc.morsecode.NDS;
import inc.morsecode.pagerduty.PDConstants.EventType;

public class PDAckEvent {
	
	private PDService service;
	private String incidentKey;
	private String description;
	private JsonObject details;

	public PDAckEvent(PDService service, String incidentKey, String description, JsonObject details) {
		this.service= service;
		this.incidentKey= incidentKey;
		this.description= description;
		this.details= details;
	}
	 
	 
	public PDService getService() {
		return service;
	}
	
	public JsonObject toJson() {
		JsonObject json= service.toJson();
		
		json.set("details", details);
		
		return json;
	}

}
