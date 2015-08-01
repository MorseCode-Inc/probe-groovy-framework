package inc.morsecode.pagerduty;

import inc.morsecode.NDS;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimRequest;
import com.nimsoft.nimbus.PDS;

public class Gateway extends Endpoint {

	
	public Gateway() {
		
	}

	
	public void GET_env(CGI cgi) throws IOException {
		cgi.setContentType("text/plain");
		
		try {
			cgi.println(probe.updateControllerInfo());
		} catch (Exception x) {
			cgi.println(x +": "+ x.getMessage());
		}
		
	}


	@Override
	public void init() {
		
	}
	

	
}
