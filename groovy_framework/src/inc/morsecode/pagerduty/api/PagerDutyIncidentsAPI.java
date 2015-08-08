package inc.morsecode.pagerduty.api;

import inc.morsecode.NDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.JsonValue;
import util.json.ex.MalformedJsonException;

public class PagerDutyIncidentsAPI {
	
	public static final String GET = "GET";
	private PDClient client;
	private static final String CONTEXT= "services";
	
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


	public List<PDService> listServices(int offset, int limit) throws IOException, MalformedJsonException {
		
		NDS params= new NDS();
		params.set("offset", offset);
		params.set("limit", limit);
		
		JsonObject data= client.call(GET, "/api/v1/services", null, params);
		
		JsonArray array= (JsonArray)data.get("services", new JsonArray());
		ArrayList<PDService> services= new ArrayList<PDService>();
		
		for(JsonValue obj : array) {
			if (obj instanceof JsonObject) {
				PDService service= new PDService(((JsonObject) obj).get("name", "invalid"), ((JsonObject) obj));
				services.add(service);
			}
		}
		
		return services;
	}
	
	
	public JsonObject triggerNewIncident(PDService service, JsonArray contexts, JsonObject alarmData) throws IOException, MalformedJsonException {
		JsonObject json= buildPdTrigger(service.getServiceKey(), alarmData, contexts);
		
		
		// String uri= "/"+ service.getServiceKey() +"/events/enqueue";
		String uri= service.getId() +"/events/enqueue";
		// HttpRequest request= client.put("/incidents", json);
		// HttpRequest request= client.buildPostRequest( , json, null);
		
		JsonObject resp= client.call("post", uri, json, null);
		// HttpResponse resp= client.execute((HttpUriRequest)request);
		return resp;
	}


	public static JsonObject buildPdTrigger(String serviceKey, JsonObject alarmData, JsonArray contexts) {
		JsonObject json= new JsonObject();
		
		// required
		json.set("service_key", serviceKey);
		json.set("event_type", "trigger");
		json.set("description", "ALert Message Description");
		
		// optional, but we will set the incident key to keep mapping back to nimsoft
		json.set("incident_key", alarmData.get("nimid"));
		
		// json.set("client", "pd_uim_gtw");
		json.set("details", alarmData);
		
		if (!contexts.isEmpty()) {
			json.set("contexts", contexts);
		}
		return json;
	}
}
