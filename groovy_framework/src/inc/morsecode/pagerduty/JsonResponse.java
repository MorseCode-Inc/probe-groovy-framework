package inc.morsecode.pagerduty;

import java.util.Map;

import util.json.JsonObject;

import com.nimsoft.nimbus.PDS;

import inc.morsecode.NDS;;

public class JsonResponse {
	
	private NDS response;
	private JsonObject payload;

	public JsonResponse(int code, String message) {
		this.response= new NDS();
		response.set("response/code", code);
		response.set("response/message", message);
		setPayload(new JsonObject());
	}

	public void setPayload(JsonObject payload) {
		this.payload= payload;
	}
	
	public void setPayload(NDS payload) {
		this.payload= payload.toJson();
	}
	
	
	public int getStatusCode() {
		return response.seek("response", "code", -1);
	}
	
	public String getMessage() {
		return response.get("response/message", "no message");
	}
	
	public NDS getNDS() {
		return response;
	}
	
	public NDS getPayload() {
		return response.getSection("payload", new NDS("payload"));
	}

	
	public String toString() {
		JsonObject json= response.toJson();
		
		json.set("payload", payload);
		
		return json.toString();
	}
	
}
