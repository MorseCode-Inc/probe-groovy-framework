package snipits;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ClientInfo;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

public class PDClient_v1 {

	
	
	
	
	public static void main(String[] args) throws Exception {
		
		
		// Create the client resource  
		// ClientResource resource = new ClientResource("http://support.morsecode-inc.com:3081/nimbus/env");  
		ClientResource resource = new ClientResource("http://support.morsecode-inc.com:3081/nimbus/alarm/create");  
		
		String domain= "morsecode-incorporated.pagerduty.com";
		// domain= "events.pagerduty.com";
		String protocol= "http";
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
		
		resource.setAttribute("Authorization", "Token token="+ apiToken);
		resource.getRequestAttributes().put("Authorization", "Token token="+ apiToken);
		
		System.out.println(resource.getRequestAttributes());
		
		resource.put(json, MediaType.APPLICATION_JSON).write(System.out);
		
	}
}
