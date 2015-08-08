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
		 set("name", name);
		 set("service_key", key);
	 }
	 

	public PDService(String name, JsonObject json) {
		super(name, json);
	}

	public String getId() { return get("id"); }
	public String getServiceKey() { return get("service_key"); }
	public String getServiceUrl() { return get("service_url"); }
	public String getName() { return get("name"); }
	public String getCreatedAt() { return get("created_at"); }
	public String getEmailFilterMode() { return get("email_filter_mode"); }
	public String getPDType() { return get("type"); }
	public int getAcknowledgement_timeout() { return get("acknowledgement_timeout", 1800); }
	public int getAutoResolveTimeout() { return get("auto_resolve_timeout", 14400); }
	public String getStatus() { return get("status"); }
	public String getEventType() { return get("event_type"); }
	public String getDescription() { return get("description"); }
	public String getIncidentKey() { return get("incident_key"); }
	public String getClient() { return get("client"); }
	public String getClientUrl() { return get("client_url"); }
	
	public NDS getIncidentCounts() {
		return seek("incident_counts", true);
	}
	
	public int getCountTotalIncidents() { return getIncidentCounts().get("total", 0); }
	public int getCountResolvedIncidents() { return getIncidentCounts().get("resolved", 0); }
	public int getCountAcknowledgedIncidents() { return getIncidentCounts().get("acknowledged", 0); }
	public int getCounttriggeredIncidents() { return getIncidentCounts().get("triggered", 0); }
	
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
		
		for (NDS nds : seek("contexts", true)) {
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
