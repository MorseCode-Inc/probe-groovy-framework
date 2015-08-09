package inc.morsecode.pagerduty;

import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDService;
import inc.morsecode.pagerduty.api.PagerDutyIncidentsAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import util.json.JsonArray;
import util.json.JsonObject;
import util.json.ex.MalformedJsonException;

public class PagerDutyAPI {

	private PDClient client;
	
	public PagerDutyAPI(String subdomain, String apiKey) {
		this.client= new PDClient(subdomain, apiKey);
	}
	
	


	/**
	 * @param args
	 * @throws MalformedJsonException 
	 * @throws IOException 
	public static void main(String[] args) throws IOException, MalformedJsonException {
		
		
		PDClient client= new PDClient("morsecode-incorporated", "pagerduty.com", "PnKQyzNjQEjsRfodeTwa");
		
		String serviceKey= "bb730aa07f74467c8254e827a0c921c8";
		
		PagerDutyServicesAPI services= new PagerDutyServicesAPI(client);
		
		services.listServices(0,50);
	}

	
	public static void main(String[] args) throws Exception {
		
		PDClient client= new PDClient("morsecode-incorporated", "pagerduty.com", "PnKQyzNjQEjsRfodeTwa");
		// PDClient client= new PDClient("services", "pagerduty.com", "PnKQyzNjQEjsRfodeTwa");
		
		
		PagerDutyServicesAPI api= new PagerDutyServicesAPI(client);
		PagerDutyIncidentsAPI incident= new PagerDutyIncidentsAPI(client);
		
		List<PDService> services= api.listAllServices();
		
		for (PDService service : services) {
			System.out.println(service);
			
			JsonObject response = incident.triggerNewIncident(service, new JsonArray(), PDClient.nimalarm());

		}
		
		
	}
	 */
	
}
