import java.io.IOException;

import util.json.ex.MalformedJsonException;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDServiceUrls;
import inc.morsecode.pagerduty.api.PagerDutyIncidentsAPI;
import inc.morsecode.pagerduty.api.PagerDutyServicesAPI;
import inc.morsecode.pagerduty.data.PDIncident;


public class ClientTestRunner {

	
	/**
	 * @param args
	 * @throws MalformedJsonException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, MalformedJsonException {

		/*
		 * 
		 */
		
		PDClient client= new PDClient("morsecode-incorporated", "PnKQyzNjQEjsRfodeTwa", new PDServiceUrls());
		
		PagerDutyIncidentsAPI incidents= new PagerDutyIncidentsAPI(client);
		PagerDutyServicesAPI services= new PagerDutyServicesAPI(client);
		
		
		PDIncident incident= incidents.getIncident("4");
		
		int count= incidents.getIncidentCount();
		
		System.out.println(incidents.getIncident("4"));
		
		
		System.out.println(services.listServices());
		
		
		
		//can have print here
	}

}
