package inc.morsecode.pagerduty.api;

import inc.morsecode.NDS;
import inc.morsecode.core.ListResult;
import inc.morsecode.nas.UIMAlarmMessage;
import inc.morsecode.pagerduty.data.PDIncident;
import inc.morsecode.pagerduty.data.PDTriggerEvent;
import inc.morsecode.pagerduty.data.PDUser;

import java.io.IOException;
import java.util.List;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.JsonValue;
import util.json.ex.MalformedJsonException;

/**
 *  &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 14, 2015
 * Project: probe-pager-duty-gateway
 * 
 *
 * Description:
 * 
 * </pre>
 * =--------------------------------=
 * @author nwhitney
 * 
 * <pre>
 * PagerDuty JSON Structure:
 *   
 * {
	  "id": "PIJ90N7",
	  "incident_number": 1,
	  "created_on": "2012-12-22T00:35:21Z",
	  "status": "triggered",
	  "pending_actions": [ 
	  	{ "type": "escalate", "at": "2014-01-01T08:00:00Z" },
	    { "type": "unacknowledge", "at": "2014-01-01T08:00:00Z" },
	    { "type": "resolve", "at": "2014-01-01T08:00:00Z" }
	  ],
	  "html_url": "https://acme.pagerduty.com/incidents/PIJ90N7",
	  "incident_key": null,
	  "service": { "id": "PBAZLIU", "name": "service", "html_url": "https://acme.pagerduty.com/services/PBAZLIU" },
	  "assigned_to_user": {  "id": "PPI9KUT", "name": "Alan Kay", "email": "alan@pagerduty.com", "html_url": "https://acme.pagerduty.com/users/PPI9KUT" },
	  "assigned_to": [
	    { 
	      "at": "2012-12-22T00:35:21Z",
	      "object": { "id": "PPI9KUT", "name": "Alan Kay", "email": "alan@pagerduty.com", "html_url": "https://acme.pagerduty.com/users/PPI9KUT", "type": "user" }
	    }
	  ],
	  "trigger_summary_data": { "subject": "45645" },
	  "trigger_details_html_url": "https://acme.pagerduty.com/incidents/PIJ90N7/log_entries/PIJ90N7",
	  "last_status_change_on": "2012-12-22T00:35:22Z",
	  "last_status_change_by": null
	}
	</pre>
 *
 */

public class PagerDutyIncidentsAPI {
	
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	private PDClient client;
	private static final String CONTEXT= "incidents";
	
	public enum Endpoints {
		LIST("GET", CONTEXT)
		, GET("GET", CONTEXT)
		, NEW("POST", CONTEXT)
		, UPDATE("PUT", CONTEXT)
		, DELETE("DELETE", CONTEXT)
		, DISABLE("PUT", CONTEXT)
		, ENABLE("PUT", CONTEXT)
		, REGENERATE("POST", CONTEXT)
		;
		
		private String httpMethod;
		private String context;
		
		Endpoints(String httpMethod, String context) {
			this.httpMethod= httpMethod;
			this.context= context;
		}
		
		public String getContext() { return context; }
		public String getHttpMethod() { return httpMethod; }
	};
	
	
	public PagerDutyIncidentsAPI(PDClient client) {
		this.client= client;
	}

	public List<PDIncident> listIncidents() throws IOException, MalformedJsonException {
		
		ListResult<PDIncident> all= listIncidents(0, 10);
		
		int total= all.getCount();
		
		if (total < 50) {
			// just get all 50 in one transaction
			return listIncidents(0, total);
		} else {
			
			// break requests into chunks
			int chunksize= 25;
			
			for (int i= 0; i < total; i+= chunksize) {
				all.addAll(listIncidents(i, chunksize));
			}
			
		}
		
		return all;
		
	}

	public ListResult<PDIncident> listIncidents(int offset, int limit) throws IOException, MalformedJsonException {
		
		NDS params= new NDS();
		params.set("offset", offset);
		params.set("limit", limit);
		
		JsonObject data= client.call(GET, "/api/v1/incidents", null, params);
		
		JsonArray array= (JsonArray)data.get("services", new JsonArray());
		ListResult<PDIncident> incidents= new ListResult<PDIncident>(data.get("total", 0));
		
		for(JsonValue obj : array) {
			if (obj instanceof JsonObject) {
				PDIncident incident= new PDIncident((JsonObject)obj);
				incidents.add(incident);
			}
		}
		
		return incidents;
	}
	
	
	public PDIncident getIncident(String id) throws IOException, MalformedJsonException {
		// Show detailed information about an incident.
		// Accepts either an incident id, or an incident number.
		// API page:
		// https://developer.pagerduty.com/documentation/rest/incidents/show
		// resource URL:
		// GET https://<subdomain>.pagerduty.com/api/v1/incidents/:id
		String uri = "/api/v1/incidents";
		
		JsonObject data= client.call(GET, uri+ "/"+ id, null, null);
		
		/*
		 * BCM: Nate, below you had this line of code:
		 * PDIncident incident= new PDIncident(data.getObject(id));
		 * 
		 * the data structure returned from pagerduty looks like this:
		 * 
		 * {
				  "id": "PIJ90N7",
				  "incident_number": 1,
				  "created_on": "2012-12-22T00:35:21Z",
				  "status": "triggered",
				  "pending_actions": [ 
				  	{ "type": "escalate", "at": "2014-01-01T08:00:00Z" },
				    { "type": "unacknowledge", "at": "2014-01-01T08:00:00Z" },
				    { "type": "resolve", "at": "2014-01-01T08:00:00Z" }
				  ],
				  "html_url": "https://acme.pagerduty.com/incidents/PIJ90N7",
				  "incident_key": null,
				  "service": { "id": "PBAZLIU", "name": "service", "html_url": "https://acme.pagerduty.com/services/PBAZLIU" },
				  "assigned_to_user": {  "id": "PPI9KUT", "name": "Alan Kay", "email": "alan@pagerduty.com", "html_url": "https://acme.pagerduty.com/users/PPI9KUT" },
				  
				  JsonData data= {}
				  PDUser assignedTo= new PDUser(data.getObject("assigned_to_user"));
				  
				  String username= assignedTo.getUserName();
				  String username= assignedTo.get("name");
				  JsonObject json= assignedTo.toJson();
				  
				  "assigned_to": [
				    { 
				      "at": "2012-12-22T00:35:21Z",
				      "object": { "id": "PPI9KUT", "name": "Alan Kay", "email": "alan@pagerduty.com", "html_url": "https://acme.pagerduty.com/users/PPI9KUT", "type": "user" }
				    }
				  ],
				  "trigger_summary_data": { "subject": "45645" },
				  "trigger_details_html_url": "https://acme.pagerduty.com/incidents/PIJ90N7/log_entries/PIJ90N7",
				  "last_status_change_on": "2012-12-22T00:35:22Z",
				  "last_status_change_by": null
				}
		 */
		// PDIncident incident= new PDIncident(data.getObject(id));
		// the JSON that is in the data variable represents a single incident
		PDIncident incident= new PDIncident(data);
		
		String incidentNumber = incident.get("id");
		
		System.out.println("Incident ID: "+ id);
		System.out.println("Incident Service Information:\n"+ incident.getService());
		System.out.println("Incident JSON:\n"+ data.toString());
		
		return incident;
	}
	
	public int getIncidentCount() throws IOException, MalformedJsonException {
		// Use this query for the count of incidents that match a given query.
		// This should be used if you don't need access to the actual incident details. 
		// API page:
		// https://developer.pagerduty.com/documentation/rest/incidents/count
		// resource URL:
		// GET https://<subdomain>.pagerduty.com/api/v1/incidents/count
		String uri = "/api/v1/incidents";
		
		JsonObject data= client.call(GET, uri+ "/count", null, null);
		
		int count= data.get("total", 0);
		//ListResult<PDIncident> all= listIncidents(0, 10);
		//int total= all.getCount();
		
		//System.out.println("incident count:"+ count);
		
		return count;
	}
	
	
	/**
	 * REF https://developer.pagerduty.com/documentation/rest/incidents/resolve
	 * 
	 * @param incident
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws MalformedJsonException
	 */
	public boolean resolve(PDIncident incident, PDUser user) throws IOException, MalformedJsonException {
		
		String uri = "/api/v1/incidents";
		// PUT https://<subdomain>.pagerduty.com/api/v1/incidents/:id/resolve
		NDS params= new NDS();
		params.set("requester_id", user.getId());
		JsonObject data= client.call(PUT, uri + "/"+ incident.getId() +"/resolve", null, params);
		
		System.out.println(data);
		
		return true;
	}
	
	/*
	public JsonObject triggerNewIncident(PDService service, JsonArray contexts, UIMAlarmMessage alarm) throws IOException, MalformedJsonException {
		JsonObject json= buildPdTrigger(service.getServiceKey(), alarm, contexts);
		
		
		// String uri= "/"+ service.getServiceKey() +"/events/enqueue";
		String uri= service.getId() +"/events/enqueue";
		// HttpRequest request= client.put("/incidents", json);
		// HttpRequest request= client.buildPostRequest( , json, null);
		
		JsonObject resp= client.call("post", uri, json, null);
		// HttpResponse resp= client.execute((HttpUriRequest)request);
		return resp;
	}
	*/


	public static JsonObject buildPdTrigger(String serviceKey, UIMAlarmMessage alarm, JsonArray contexts) {
		JsonObject json= new JsonObject();
		
		// required
		json.set("service_key", serviceKey);
		json.set("event_type", "trigger");
		json.set("description", "Alert Message Description");
		
		// optional, but we will set the incident key to keep mapping back to nimsoft
		json.set("incident_key", alarm.getNimid());
		
		// json.set("client", "pd_uim_gtw");
		
		// turn the alarm message into a JsonObject
		JsonObject details= alarm.getBody().toJson();
		JsonObject routing= new JsonObject();
		
		// append the UIM routing information
		for (String key : alarm.keys()) {
			routing.set(key, alarm.get(key));
		}
		
		details.set("routing", routing);
		
		json.set("details", details);
		
		if (!contexts.isEmpty()) {
			json.set("contexts", contexts);
		}
		return json;
	}
	

	public JsonObject send(PDTriggerEvent event) throws IOException, MalformedJsonException {
		
		
		// String uri= "/"+ service.getServiceKey() +"/events/enqueue";
		String uri= event.getService().getServiceKey() +"/events/enqueue";
		
		uri= client.urls().getEventTrigger(client, event);
		
		// HttpRequest request= client.put("/incidents", json);
		// HttpRequest request= client.buildPostRequest( , json, null);
		//uri= "https://events.pagerduty.com/generic/2010-04-15/create_event.json";
		//uri= "https://redonkulizer-toy.herokuapp.com/redonkulize/passthrough/"+ event.getServiceKey();
		//uri= triggerUrl(event);
		System.out.println("SEND TRIGGER:\n"+ event);
		
		JsonObject resp= client.call("post", uri, event.toJson(), null, false);
		// HttpResponse resp= client.execute((HttpUriRequest)request);
		System.out.println("RESPONSE: \n"+ resp);
		return resp;
	}

	public void update(IncidentUpdateParameters params) {
		
	}
}
