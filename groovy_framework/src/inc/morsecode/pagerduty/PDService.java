package inc.morsecode.pagerduty;

/**
 * 
 * @author bcmorse
 * 
 * Reference Material:
 * https://developer.pagerduty.com/documentation/integration/events/trigger
 */

import util.json.JsonArray;
import util.json.JsonObject;
import inc.morsecode.NDS;
import inc.morsecode.pagerduty.PDConstants.EventType;

public class PDService extends NDS {
	
	private NDS details= new NDS("details");
	
	 public PDService(String name, String key) {
		 this(name, key, EventType.TRIGGER);
	 }
	 

	
	public String getEventType() {
		return get("event_type");
	}
	
	public String getDescription() {
		return get("description");
	}
	
	public String getIncidentKey() {
		return get("incident_key");
	}
	
	public String getClient() {
		return get("client");
	}
	
	public String getClientUrl() {
		return get("client_url");
	}
	
	public JsonObject getDetails() {
		JsonObject details= new JsonObject();
		
		// set any attributes we want on the JSON object
		
		return details;
	}
	
	/*
	 * Contexts to be included with the incident trigger such as links to graphs or images.
	 * A "type" is required for each context submitted. For type "link", an "href" is
	 * required. You may optionally specify "text" with more information about the link.
	 * For type "image", "src" must be specified with the image src. You may optionally
	 * specify an "href" or an "alt" with this image.
	 */
	public JsonArray getContexts() {
		JsonArray contexts= new JsonArray();
		
		for (NDS nds : seek("contexts")) {
			contexts.add(nds.toJson());
		}
		
		return contexts;
	}
	
	public void addContext(PDContext context) {
		super.seek("contexts", true).add(context);
	}

	public void setDetails(NDS details) {
		this.details= details;
		set("details", details);
	}
	
	
	@Override
	public JsonObject toJson() {
		JsonObject json= super.toJson();
		
		json.set("contexts", getContexts());
		
		return json;
	}

	
}
