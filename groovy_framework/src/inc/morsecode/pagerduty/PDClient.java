package inc.morsecode.pagerduty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.morsecode.NDS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.sun.xml.internal.ws.message.source.ProtocolSourceMessage;

public class PDClient {

	private NDS data= new NDS("pagerduty_client");
	private NDS services;
	private Client restlet= new Client(Protocol.HTTPS);
	
	public PDClient(String subdomain, String apiKey) {
		this(subdomain, "pagerduty.com", apiKey);
	}
	public PDClient(String subdomain, String domain, String apiKey) {
		this.services= data.seek("services", true);
		data.set("subdomain", subdomain);
		data.set("domain", domain);
		data.set("auth/api_key", apiKey);
		
		
		
		List<Protocol> protocols= new ArrayList<Protocol>();
		protocols.add(Protocol.HTTPS);
		protocols.add(Protocol.HTTP);
		restlet= new Client(protocols);
		
		
	}
	
	public String getSubdomain() {
		return data.get("subdomain", "events");
	}
	
	public String getDomain() {
		return getSubdomain() +"."+ getTopLevelDomain();
	}
	
	public String getTopLevelDomain() {
		return data.get("domain", "pagerduty.com");
	}
	
	public String getBaseUrl(String protocol, String apiVer) {
		return (protocol +"://"+ getDomain() +"/api/"+ apiVer);
	}
	
	public String getApiToken() {
		return data.get("api_token", (String)null);
	}
	
	public void addService(String name, String token) {
		NDS service= services.seek(name, true);
		service.set("name", name);
		service.set("token", token);
	}
	
	public String getServiceToken(String name) {
		return services.get(name +"/token", (String) null);
	}
	

    public Request put(String uri, JSONObject data) {
    	return this.http(Method.PUT, uri, data);
    }
    
    private Request http(Method method, String uri, JSONObject data) {
    	Request request= new Request(method, getBaseUrl("http", "v1") + uri);
    	
    	// request.setResourceRef(getBaseUrl("http", "v1"));
    	
    	ChallengeScheme sc= ChallengeScheme.CUSTOM;
    	
    	Engine.getInstance().getRegisteredAuthenticators().add(new PagerDutyAuthenticationHelper());
    	
    	ChallengeResponse cr= new ChallengeResponse(new PagerDutyAuthenticationHelper().getChallengeScheme(), "Token", "token="+ getApiToken());
    	
    	request.setChallengeResponse(cr);
    	Series<Header> headers= new Series<Header>(Header.class);
    	headers.add(new Header("Authorization", "Token token="+ getApiToken()));
    	HeaderUtils.addRequestHeaders(request, headers);
    	
    	
    	
    	// setHttpHeader(request, "Authorization", "Token token="+ getApiToken());
    	
    	
    	// request.getResourceRef().set
    	
    	return request;
    }
    
    public org.restlet.Response execute(Request request, JSONObject json) {
    	try {
    		System.out.println("__");
    		System.out.println(request);
    		System.out.println("__");
    		request.setEntity(json.toString(), MediaType.APPLICATION_JSON);
    		return restlet.handle(request);
    	} catch (IllegalArgumentException iax) {
    		System.err.println("ILLEGAL ARGUMENT:\n"+ iax);
    	} catch (NullPointerException npx) {
    		System.err.println("NULL:\n"+ npx);
    	} catch (Throwable error) {
    		System.err.println("ERROR:\n"+ error);
    		
    	} finally {
    		
    	System.out.println("HERE");
    	System.out.flush();
    	System.err.flush();
    	}
    	
    	return null;
    }
	
	public static void main(String[] args) throws Exception {
		
		PDClient client= new PDClient("morsecode-incorporated", "pagerduty.com", "PnKQyzNjQEjsRfodeTwa");
		
		JSONObject json= new JSONObject();
		JSONObject alarmData= new JSONObject();
		JSONArray contexts= new JSONArray();
		
		alarmData.put("nimid", "11233333ABAB");
		alarmData.put("robot", "robot-name");
		alarmData.put("source", "alarm-source");
		alarmData.put("subsystem", "2.11.1");
		alarmData.put("origin", "origin");
		alarmData.put("assigned_to", "assigned_to");
		alarmData.put("assigned_by", "assigned_by");
		alarmData.put("assigned_ts", "2015-01-01 14:44:00");
		alarmData.put("nimts", "2015-01-01 14:44:00");
		alarmData.put("arrival_ts", "2015-01-01 14:44:00");
		alarmData.put("last_received_ts", "2015-01-01 14:44:00");
		alarmData.put("origin_ts", "2015-01-01 14:44:00");
		
		json.put("service_key", "bb730aa07f74467c8254e827a0c921c8");
		json.put("event_type", "trigger");
		json.put("description", "ALert Message Description");
		// json.put("incident_key", null);
		json.put("client", "pd_uim_gtw");
		//json.put("client_url", null);
		json.put("details", alarmData);
		json.put("contexts", contexts);
		
		
		Request request= client.put("/incidents", json);
		
		Response resp= client.execute(request, json);
		
		if (resp != null) {
		
			resp.getEntity().write(System.out);
		} else {
			// response was null
			
		}
		
		
	}
	
	public static void main1(String[] args) throws Exception {
		
		
		// Create the client resource  
		// ClientResource resource = new ClientResource("http://support.morsecode-inc.com:3081/nimbus/env");  
		ClientResource resource = new ClientResource("http://support.morsecode-inc.com:3081/nimbus/alarm/create");  

		String domain= "morsecode-incorporated.pagerduty.com";
		// domain= "events.pagerduty.com";
		String protocol= "https";
		String pdUrl= protocol +"://"+ domain +"/api/v1/";
		resource= new ClientResource(pdUrl + "incidents");
		// resource= new ClientResource(pdUrl + "generic/2010-04-15/create_event.json");
		
		resource.setMethod(Method.PUT);

		JSONObject json= new JSONObject();
		JSONObject alarmData= new JSONObject();
		JSONArray contexts= new JSONArray();
		
		alarmData.put("nimid", "11233333ABAB");
		alarmData.put("robot", "robot-name");
		alarmData.put("source", "alarm-source");
		alarmData.put("subsystem", "2.11.1");
		alarmData.put("origin", "origin");
		alarmData.put("assigned_to", "assigned_to");
		alarmData.put("assigned_by", "assigned_by");
		alarmData.put("assigned_ts", "2015-01-01 14:44:00");
		alarmData.put("nimts", "2015-01-01 14:44:00");
		alarmData.put("arrival_ts", "2015-01-01 14:44:00");
		alarmData.put("last_received_ts", "2015-01-01 14:44:00");
		alarmData.put("origin_ts", "2015-01-01 14:44:00");
		
		json.put("service_key", "bb730aa07f74467c8254e827a0c921c8");
		json.put("event_type", "trigger");
		json.put("description", "ALert Message Description");
		// json.put("incident_key", null);
		json.put("client", "pd_uim_gtw");
		//json.put("client_url", null);
		json.put("details", alarmData);
		json.put("contexts", contexts);
		
		String apiToken= "PnKQyzNjQEjsRfodeTwa";
		
		JsonRepresentation r=new JsonRepresentation(json);
		
		r.write(System.out);
		System.out.println();
		System.out.println(MediaType.APPLICATION_JSON);
		
		
		resource.put(json, MediaType.APPLICATION_JSON).write(System.out);
		
	}
}
