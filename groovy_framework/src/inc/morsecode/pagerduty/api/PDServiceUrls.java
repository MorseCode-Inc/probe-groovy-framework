package inc.morsecode.pagerduty.api;

import java.util.HashMap;
import java.util.Map;

import util.StrUtils;
import util.json.JsonObject;
import inc.morsecode.NDS;
import inc.morsecode.pagerduty.data.PDIncident;
import inc.morsecode.pagerduty.data.PDTriggerEvent;
import inc.morsecode.pagerduty.data.PDUser;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 24, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 *    
   &lt;service_urls&gt;
   	&lt;event&gt;
   		x_trigger= https://events.pagerduty.com/generic/2010-04-15/create_event.json
   		x_trigger= https://$domain/redonkulize/passthrough/$event.service_key"+ event.getServiceKey();
   		trigger= https://redonkulizer-toy.herokuapp.com/redonkulize/passthrough/$event.service_key"+ event.getServiceKey();
   	&lt;/event&gt;
   	&lt;incident&gt;
   		api= https://$domain/api/v1/incidents
   		resolve= https://$domain/api/v1/incidents/$incident.id/resolve
   		ack= https://$domain/api/v1/incidents/$incident.id/acknowledge
   		reassign= incidents/$incident.id/reassign
   		snooze= incidents/$incident.id/snooze
   	&lt;/incident&gt;
   	&lt;service&gt;
   		api= /api/v1/services
   		create= /api/v1/services
   		update= /api/v1/services/$service.id
   		delete= /api/v1/services/$service.id
   		disable= /api/v1/services/$service.id/disable
   		enable= /api/v1/services/$service.id/enable
   	&lt;/service&gt;
   	
   &lt;/service_urls&gt;
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class PDServiceUrls extends NDS {

	private static final String SERVICE_PUT_RESOLVE = "service/put:resolve";
	private static final String SERVICE_GET_COUNT = "service/get:count";
	private static final String SERVICE_GET_INCIDENT = "service/get:incident";
	private static final String SERVICE_POST_CREATE = "service/post:create";
	private static final String SERVICE_API = "service/api";
	
	private HashMap<String, String> cache= new HashMap<String, String>();
	
	public PDServiceUrls() { super("service_urls"); }
	public PDServiceUrls(String name) { super(name); }
	public PDServiceUrls(Map<String, Object> map) { super(map); }
	public PDServiceUrls(NDS nds) { super(nds); }
	public PDServiceUrls(String name, Map<String, Object> map) { super(name, map); }
	public PDServiceUrls(NDS nds, boolean reference) { super(nds, reference); }
	public PDServiceUrls(String name, JsonObject json) { super(name, json); }

	
	public String getEventTriggerTemplate() {
		return get("event/trigger", "https://events.pagerduty.com/generic/2010-04-15/create_event.json");
	}
	
	public String getEventTrigger(PDClient client, PDTriggerEvent event) {
		String url= get("event/trigger", "https://events.pagerduty.com/generic/2010-04-15/create_event.json");
		// String url= "https://redonkulizer-toy.herokuapp.com/redonkulize/passthrough/"+ event.getServiceKey();
		
		url= prepareUrl(client, event, url, "event");
		
		return url;
	}
	
	/**
	 * 
	 * https://$domain/api/v1/incidents
	 * @return
	 */
	
	public String getServiceApi() { return get(SERVICE_API, "https://$domain/api/v1/services"); }
	public String getServiceCreate() { return get(SERVICE_POST_CREATE, "http://$domain/api/v1/services"); }
	public String getService() { return get(SERVICE_GET_INCIDENT, "http://$domain/api/v1/services/$incident.id"); }
	public String getServiceCount() { return get(SERVICE_GET_COUNT, "http://$domain/api/v1/services/count"); }
	public String getServiceResolve() { return get(SERVICE_PUT_RESOLVE, "/api/v1/$incident.id/resolve"); }
	public String getServiceUpdate() { return get("service/put:update", "http://$domain/api/v1/services"); }
	public String getServiceAck() { return get("service/put:ack", "/api/v1/$incident.id/acknowledge"); }
	public String getServiceReassign() { return get("service/put:reassign", "/api/v1/$incident.id/reassign"); }
	public String getServiceSnooze() { return get("service/put:snooze", "/api/v1/$incident.id/snooze"); }
	
	public String getServiceList(PDClient client) {
		String url= get("service/get:list", "https://$domain/api/v1/services");
		
		url= prepareUrl(client, url);
		
		return url;
	}
	
	
	
	public String getIncidentApi(PDClient client) { 
		String url= get("incident/api", "https://$domain/api/v1/incidents"); 
		url= prepareUrl(client, url);
		return url;
	}
	
	public String getIncidentCount() { return get("incident/get:count", "http://$domain/api/v1/incidents/count"); }
	public String getIncidentResolve() { return get("incident/put:resolve", "/api/v1/$incident.id/resolve"); }
	
	public String getIncidentUpdate(PDClient client) { 
		String url= get("incident/put:update", "/api/v1/incidents"); 
		return prepareUrl(client, url);
	}
	
	public String getIncidentAck() { return get("incident/put:ack", "/api/v1/$incident.id/acknowledge"); }
	public String getIncidentReassign() { return get("incident/put:reassign", "/api/v1/$incident.id/reassign"); }
	
	public String getIncidentSnooze(PDClient client, PDIncident incident) { 
		String url= get("incident/put:snooze", "/api/v1/$incident.id/snooze");
		url= prepareUrl(client, incident, url, "incident");
		return url;
	}
	
	// public String getIncidentList() { return get("incident/get:list", "http://$domain/api/v1/incidents"); }
	public String getIncidentList(PDClient client) { 
		String url= get("incident/get:list", "/api/v1/incidents");
		return prepareUrl(client, url);
	}
	
	// protected String getIncident() { return get("incident/get:incident", "http://$domain/api/v1/incidents/$incident.id"); }
	
	public String getIncident(PDClient client, String incidentId) { 
		return getIncident(client, new PDIncident(incidentId));
	}
	
	public String getIncident(PDClient client, PDIncident incident) { 
		String url= get("incident/get:incident", "/api/v1/incidents/$incident.id"); 
		String object = "incident";
		return prepareUrl(client, incident, url, object);
	}
	
	public String getUser(PDClient client, PDUser user) { 
		String url= get("user/get:user", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserOnCall(PDClient client, PDUser user) { 
		String url= get("user/get:oncall", "/api/v1/users/$user.id/on_call");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserCreate(PDClient client, PDUser user) { 
		String url= get("user/post:create", "/api/v1/users");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserList(PDClient client) { 
		String url= get("user/get:list", "/api/v1/users");
		return prepareUrl(client, url);
	}
	
	public String getUserUpdate(PDClient client, PDUser user) { 
		String url= get("user/put:update", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserDelete(PDClient client, PDUser user) { 
		String url= get("user/delete:user", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String prepareUrl(PDClient client, NDS data, String url, String object) {
		NDS vars= new NDS();
		
		for (String key : data.keys()) {
			String value= data.get(key);
			vars.set("\\$"+ object +"."+ key, value);
		}
		
		url= prepareUrl(client, url);
		
		return substitute(url, vars);
	}
	
	public String prepareUrl(PDClient client, String url) {
		
		String hash= StrUtils.SHA(client +":"+ url);
		String cached= cache.get(hash);
		if (cached != null) {
			return cached;
		}
		
		NDS vars= new NDS();
		
		vars.set("\\$domain", client.getDomain());
		vars.set("\\$subdomain", client.getSubdomain());
		vars.set("\\$tld", client.getTopLevelDomain());
		url= substitute(url, vars);
		if (cache.size() > 5000) { cache.clear(); }
		cache.put(hash, url);
		return url;
	}
	
	public String substitute(String url, NDS vars) {
		for (String key : vars.keys()) {
			url= url.replaceAll(key, vars.get(key));
		}
		
		return url;
	}
}
