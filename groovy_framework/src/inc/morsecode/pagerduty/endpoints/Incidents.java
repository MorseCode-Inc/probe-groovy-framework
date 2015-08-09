package inc.morsecode.pagerduty.endpoints;

import java.io.IOException;

import inc.morsecode.NDS;
import inc.morsecode.core.Endpoint;
import inc.morsecode.pagerduty.Probe;
import inc.morsecode.probes.http_gateway.CGI;

public class Incidents extends Endpoint {
	
	private Probe probe;

	public Incidents() {
	}

	@Override
	public void init() {

	}
	
	public void setProbe(Probe probe, NDS config) {
		super.setProbe(probe, config);
		this.probe= probe;
	}

	public void GET_index(CGI cgi) throws IOException {
		cgi.println("<html>");
		cgi.println("<title>"+ this.getClass().getSimpleName() +"</title>");
		cgi.println("<h3>"+ this.getClass().getSimpleName() +"</h3>");
		cgi.println("</html>");
	}
	
	
}
