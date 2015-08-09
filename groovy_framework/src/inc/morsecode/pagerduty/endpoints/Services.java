package inc.morsecode.pagerduty.endpoints;

import java.io.IOException;
import java.util.List;

import util.json.ex.MalformedJsonException;

import inc.morsecode.NDS;
import inc.morsecode.core.Endpoint;
import inc.morsecode.core.HttpGateway;
import inc.morsecode.pagerduty.Probe;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PDService;
import inc.morsecode.probes.http_gateway.CGI;

public class Services extends Endpoint {
	
	private Probe probe;

	public Services() {
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
		cgi.println("<title>"+ this.getClass().getSimpleName() +"</title>");
		cgi.println("<h3>"+ this.getClass().getSimpleName() +"</h3>");
		cgi.println("</html>");
	}
	
	
	public void GET_list(CGI cgi) throws IOException, MalformedJsonException {
		
		PDClient client= probe.getClient().newInstance();
		
		List<PDService> services= client.incidents().listServices();
		
		cgi.println("<table>");
		for (PDService service : services) {
			
			cgi.println("<tr>");
			cgi.println("<td>");
			cgi.println(service.getName());
			cgi.println("</td>");
			cgi.println("<td><pre>");
			cgi.println(service.getServiceKey());
			cgi.println("</pre></td>");
			cgi.println("<td><pre>");
			cgi.println(service.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
			cgi.println("</pre></td>");
			cgi.println("</tr>");
		}
		cgi.println("</table>");
	}
	
}
