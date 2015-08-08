package inc.morsecode.pagerduty;

import inc.morsecode.NDS;
import inc.morsecode.core.ListResult;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDService;

import java.io.IOException;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.JsonValue;
import util.json.ex.MalformedJsonException;

public class PagerDutyServicesAPI {
	
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
	
	
	public PagerDutyServicesAPI(PDClient client) {
		this.client= client;
	}

	public ListResult<PDService> listAllServices() throws IOException, MalformedJsonException {
		
		int offset= 0;
		int limit= 1;
		
		ListResult<PDService> all= listServices(offset, limit);
		
		offset+= limit;
		if (all.getCount() > limit) {
			
			for (int i= 1; i < all.getCount(); i++) {
				
				ListResult<PDService> page= listServices(offset, limit);
				all.addAll(page);
				
				offset+= limit;
			}
			
		}
		
		
		
		return all;
	}

	public ListResult<PDService> listServices() throws IOException, MalformedJsonException {
		return listServices(0, 25);
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
	

}
