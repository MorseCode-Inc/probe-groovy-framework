import java.io.IOException;

import util.json.ex.MalformedJsonException;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PagerDutyIncidentsAPI;


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
		
		PDClient client= new PDClient("morsecode-incorporated", "PnKQyzNjQEjsRfodeTwa");
		
		PagerDutyIncidentsAPI incidents= new PagerDutyIncidentsAPI(client);
		
		
		incidents.getIncident("4");
		incidents.getIncidentCount();
		
		
		System.out.println(incidents.getIncident("4").getAssignedTo());
		
		System.out.println(incidents.getIncident("4").getAssignedTo().getFistAssignedUser());
		
		
		//can have print here
	}

}
