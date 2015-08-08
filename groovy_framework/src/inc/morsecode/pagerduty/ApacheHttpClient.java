package inc.morsecode.pagerduty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApacheHttpClient {

	/**
	 * @param args
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws JSONException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
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
		

		HttpPut put= new HttpPut("http://morsecode-incorporated.pagerduty.com/api/v1/incidents");
		
		put.setHeader("Authorization", "Token token=12354");
		put.setEntity(new StringEntity(json.toString()));
		
		HttpClientBuilder b= HttpClientBuilder.create();
		try {
			System.out.println(b.build().execute(put));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
