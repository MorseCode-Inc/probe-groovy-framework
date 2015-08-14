package inc.morsecode.pagerduty.api;

import java.util.Map;

import util.json.JsonObject;

import inc.morsecode.NDS;
import inc.morsecode.nas.UIMAlarmMessage;
import inc.morsecode.pagerduty.Probe;

public class PDTriggerEvent {
	
	private PDService service;
	private NDS data;
	
	public static final String SERVICE_KEY= "service_key";
	public static final String EVENT_TYPE= "event_type";
	public static final String DESCRIPTION= "description";
	public static final String INCIDENT_KEY= "incident_key";
	public static final String CLIENT= "client";
	public static final String CLIENT_URL= "client_url";
	public static final String DETAILS= "details";
	public static final String CONTEXTS= "contexts";

	public PDTriggerEvent(PDService service, UIMAlarmMessage alarm) {
		this.data= new NDS();
		this.service= service;
		
		setEventType("trigger");
		setServiceKey(service.getServiceKey());
		setIncidentKey(alarm.getAlarmSuppKey());
		setDescription(alarm.getAlarmMessage());
		setDetails(alarm.getBody());
		setClient(Probe.PROBE_NAME +"/"+ Probe.PROBE_VERSION);
		
	}
	
	public PDService getService() {
		return service;
	}
	
	
	public JsonObject toJson() {
		JsonObject json= data.toJson();
		return json;
	}

	
	public String getServiceKey() { return data.get(SERVICE_KEY); }
	
	public String getEventType() { return data.get(SERVICE_KEY); }
	public String getDescription() { return data.get(SERVICE_KEY); }
	public String getIncidentKey() { return data.get(SERVICE_KEY); }
	
	public String getClient() { return data.get(SERVICE_KEY); }
	public String getClientUrl() { return data.get(SERVICE_KEY); }
	
	public NDS getDetails() { return data.seek(DETAILS, true); }
	public NDS getContexts() { return data.seek(CONTEXTS, true); }
	
	public void set(String key, String value) {
		data.set(key, value);
	}
	
	public void setServiceKey(String value) { set(SERVICE_KEY, value); }
	public void setEventType(String value) { set(EVENT_TYPE, value); }
	public void setDescription(String value) { set(DESCRIPTION, value); }
	public void setIncidentKey(String value) { set(INCIDENT_KEY, value); }
	public void setClient(String value) { set(CLIENT, value); }
	public void setClientUrl(String value) { set(CLIENT_URL, value); }
	public void setDetails(NDS value) { 
		NDS nds= new NDS(value, false);		// make a copy, true copy
		nds.setName("details");
		data.set(DETAILS, nds);
	}
	
	public String toString() {
		return data.toJson().toString();
	}
}
