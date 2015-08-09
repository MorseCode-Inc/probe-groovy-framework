package inc.morsecode.pagerduty;

import java.util.Map;

import util.json.JsonObject;
import inc.morsecode.NDS;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 9, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * 
 * 
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class AlarmServiceFilter extends NDS {

	public AlarmServiceFilter(NDS nds) {
		super(nds);
	}

	public AlarmServiceFilter(NDS nds, boolean reference) {
		super(nds, reference);
	}

	public AlarmServiceFilter(String name, JsonObject json) {
		super(name, json);
	}
	
	
	

}
