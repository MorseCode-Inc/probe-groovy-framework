package inc.morsecode.pagerduty.api;

import inc.morsecode.NDS;
import inc.morsecode.core.ListResult;
import inc.morsecode.nas.UIMAlarmMessage;

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

	public List<PDService> listServices() throws IOException, MalformedJsonException {
		
		ListResult<PDService> all= listServices(0, 1);
		
		int total= all.getCount();
		
		if (total < 50) {
			// just get all 50 in one transaction
			return listServices(0, total);
		} else {
			
			// break requests into chunks
			int chunksize= 25;
			
			for (int i= 0; i < total; i+= chunksize) {
				all.addAll(listServices(i, chunksize));
			}
			
		}
		
		return all;
		
	}

	public ListResult<PDService> listServices(int offset, int limit) throws IOException, MalformedJsonException {
		
		NDS params= new NDS();
		params.set("offset", offset);
		params.set("limit", limit);
		
		JsonObject data= client.call(GET, "/api/v1/services", null, params);
		
		JsonArray array= (JsonArray)data.get("services", new JsonArray());
		ListResult<PDService> services= new ListResult<PDService>(data.get("total", 0));
		
		for(JsonValue obj : array) {
			if (obj instanceof JsonObject) {
				PDService service= new PDService(((JsonObject) obj).get("name", "invalid"), ((JsonObject) obj));
				services.add(service);
			}
		}
		
		return services;
	}
	
	
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


	public static JsonObject buildPdTrigger(String serviceKey, UIMAlarmMessage alarm, JsonArray contexts) {
		JsonObject json= new JsonObject();
		
		// required
		json.set("service_key", serviceKey);
		json.set("event_type", "trigger");
		json.set("description", "ALert Message Description");
		
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
		// HttpRequest request= client.put("/incidents", json);
		// HttpRequest request= client.buildPostRequest( , json, null);
		System.out.println("SEND TRIGGER:\n"+ event);
		
		JsonObject resp= client.call("post", uri, event.toJson(), null);
		// HttpResponse resp= client.execute((HttpUriRequest)request);
		System.out.println("RESPONSE: \n"+ resp);
		return resp;
	}
}
