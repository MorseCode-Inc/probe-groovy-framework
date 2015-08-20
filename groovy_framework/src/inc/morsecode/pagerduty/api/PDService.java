package inc.morsecode.pagerduty.api;

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

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 8, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class PDService extends NDS {
	
	private NDS details= new NDS("details");
	
	
	 public PDService(String name, String key) {
		 set("name", name);
		 set("service_key", key);
	 }
	 

	 /**
	  * <pre>
	  * outpost>
	  *   scheduled_actions = []
	  *   service_url = /services/PAPCXT0
	  *   status = active
	  *   auto_resolve_timeout = 14400
	  *   last_incident_timestamp = 2015-08-02T16:58:37-06:00
	  *   email_filter_mode = all-email
	  *   acknowledgement_timeout = 1800
	  *   type = generic_events_api
	  *   id = PAPCXT0
	  *   service_key = c64252179f5b4acda00d83091dadc17a
	  *   description = null
	  *   name = outpost
	  *   created_at = 2015-08-02T16:50:12-06:00
	  * incident_counts>
	  * 	total = 1
	  * 	resolved = 1
	  * 	acknowledged = 0
	  * 	triggered = 0
	  * /incident_counts>
	  * incident_urgency_rule>
	  * 	type = constant
	  * 	urgency = high
	  * /incident_urgency_rule>
	  * /outpost>
	  * 
	  * </pre>
	  * @param name
	  * @param json
	  */
	public PDService(String name, JsonObject json) {
		super(name, json);
	}

	// PagerDuty GET services JSON API calls for
	// https://developer.pagerduty.com/documentation/rest/services/list
	public String getId() { return get("id"); }
	public String getServiceKey() { return get("service_key"); }
	public String getServiceUrl() { return get("service_url"); }
	public String getName() { return get("name"); }
	public String getCreatedAt() { return get("created_at"); }
	
	public NDS getEscalationPolicy() { return seek("escalation_policy", true); }
	public String getEmailFilterMode() { return get("email_filter_mode"); }
	public NDS getEmailFilters() { return seek("email_filters", true); }
	public String getPDType() { return get("type"); }
	public int getAcknowledgementTimeout() { return get("acknowledgement_timeout", 1800); }
	public int getAutoResolveTimeout() { return get("auto_resolve_timeout", 14400); }
	public String getStatus() { return get("status"); }
	
	public String getLastIncidentTimeStamp() { return get("last_incident_timestamp"); }
	public String getEmailIncidentCreation() { return get("email_incident_creation"); }
	public String getSeverityFilter() { return get("severity_filter"); }
	
	// Parameters, as opposed to Response Fields
	public String getTeams() { return get("teams"); }
	public String getTimeZone() { return get("time_zone"); }
	public String getQuery() { return get("query"); }
	public String getSortBy() { return get("sort_by"); }
	
	// PagerDuty GET services/:id
	// https://developer.pagerduty.com/documentation/rest/services/show
	// Include extra information in the response.
	// Possible values are escalation_policy and email_filters
	public String getIncludeExtraInfo() { return get("include"); }
	
	// PagerDuty JSON API calls for
	// https://developer.pagerduty.com/documentation/integration/events/trigger
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
	public int getCountTriggeredIncidents() { return getIncidentCounts().get("triggered", 0); }
	
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
