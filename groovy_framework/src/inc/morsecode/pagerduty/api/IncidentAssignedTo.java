package inc.morsecode.pagerduty.api;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import sun.java2d.pipe.SpanShapeRenderer.Simple;
import util.json.JsonObject;
import util.kits.DateKit;
import util.kits.SimpleCalendar;
import groovy.json.internal.SimpleCache;
import inc.morsecode.NDS;

/**
 * 
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 15, 2015
 * Project: probe-pager-duty-gateway
 * 
 *
 * Description:
 * "assigned_to":
		 [
		 	{
		 	"at":"2015-08-15T17:57:13Z",
		    	"object":{
		 	 		"html_url":"https://morsecode-incorporated.pagerduty.com/users/PAGZ2F9"
		 	 		, "id":"PAGZ2F9"
		 	 		, "email":"brad@morsecode-inc.com"
		 	 		, "name":"Brad Morse"
		 	 		, "type":"user"
		 	 	}
		 	}
		 ]
 * 
 * </pre>
 * =--------------------------------=
 * @author bmorse
 *
 */
public class IncidentAssignedTo extends NDS {

	public IncidentAssignedTo() {
	}

	public IncidentAssignedTo(String name) {
		super(name);
	}

	public IncidentAssignedTo(Map<String, Object> map) {
		super(map);
	}

	public IncidentAssignedTo(NDS nds) {
		super(nds);
	}

	public IncidentAssignedTo(String name, Map<String, Object> map) {
		super(name, map);
	}

	public IncidentAssignedTo(NDS nds, boolean reference) {
		super(nds, reference);
	}

	public IncidentAssignedTo(String name, JsonObject json) {
		super(name, json);
	}
	
	
	public SimpleCalendar getFistAssignmentTime() {
		
		for (NDS element : this) {
			SimpleCalendar date= new SimpleCalendar(1970);
		
			SimpleCalendar timestamp= this.get("at", "yyyy-MM-dd'T'HH:mm:ssz", date);
			
			return timestamp;
		}
		
		return null;
	}
	
	public PDUser getFistAssignedUser() {
		
		for (NDS element : this) {
			
			NDS object= this.seek("object");
			if (object == null) {
				return null;
			}
			
			return new PDUser(object);
		}
		
		return null;
	}

	private SimpleCalendar get(String key, String fmt, SimpleCalendar ifNull) {
		
		String value= this.get("key");
		
		if (ifNull != null) {
			value= this.get(key, DateKit.format(ifNull, fmt));
		}
		
		Date date;
		try {
			date= DateKit.toDate(value, fmt);
			return new SimpleCalendar(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return ifNull;
		}
		
	}
	

}
