package inc.morsecode.pagerduty.endpoints;

import java.io.IOException;

import inc.morsecode.NDS;
import inc.morsecode.core.Endpoint;
import inc.morsecode.core.HttpGateway;
import inc.morsecode.pagerduty.Probe;
import inc.morsecode.probes.http_gateway.CGI;

public class ProbeAdmin extends Endpoint {
	
	private Probe probe;

	public ProbeAdmin() {
	}

	@Override
	public void init() {

	}
	
	@Override
	public void setProbe(HttpGateway probe, NDS config) {
		super.setProbe(probe, config);
		this.probe= (Probe)probe;
	}
	

	public void GET_index(CGI cgi) throws IOException {
		cgi.println("<html>");
		
		NDS config= probe.getConfig();
		
		cgi.println("<table>");
		
		for (NDS section : config) {
			cgi.println("<tr><td>"+ section.getName() +"</td><td><pre>"+ section.toString().replaceAll("<",  "&lt;").replaceAll(">", "&gt;")+"</pre></td></tr>");
		}
		
		cgi.println("</table>");
		
		cgi.println("</html>");
	}
	
	
}
