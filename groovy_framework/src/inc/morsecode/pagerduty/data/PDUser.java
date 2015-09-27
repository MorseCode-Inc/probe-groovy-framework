package inc.morsecode.pagerduty.data;

import java.util.Map;

import util.json.JsonObject;
import inc.morsecode.NDS;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 14, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * 
 * {
 * 	"id": "PPI9KUT",
 * 	"name": "Alan Kay",
 * 	"email": "alan@pagerduty.com",
 * 	"html_url": "https://acme.pagerduty.com/users/PPI9KUT"
 * }
 * 
 * </pre></br>
 * =--------------------------------=
 * 
 * 
 * 
 * 
 * 
 */
public class PDUser extends NDS {

	public PDUser() {
	}

	public PDUser(String userid) {
		super("user");
		set("id", userid);
	}

	public PDUser(Map<String, Object> map) {
		super(map);
	}

	public PDUser(NDS nds) {
		super(nds);
	}

	public PDUser(String name, Map<String, Object> map) {
		super(name, map);
	}

	public PDUser(NDS nds, boolean reference) {
		super(nds, reference);
	}

	public PDUser(String name, JsonObject json) {
		super(name, json);
	}

	
	public String getUserEmail() { return this.get("email"); }
	public void setUserEmail(String name) { this.set("email", name); }
	
	public String getUserName() { return this.get("name"); }
	public void setUserName(String name) { this.set("name", name); }

	public String getId() { return this.get("id"); }
	
}
