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
		 * Nate: 
		 * I wrote this as a 
		 * 
		 * 
		 * 
		 */
		
		PDClient client= new PDClient("morsecode-incorporated", "PnKQyzNjQEjsRfodeTwa");
		
		PagerDutyIncidentsAPI incidents= new PagerDutyIncidentsAPI(client);
		
		
		incidents.getIncident("1");
	}

}
