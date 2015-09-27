package inc.morsecode.pagerduty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import util.json.JsonObject;
import inc.morsecode.NDS;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 30, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * 
 *    
   &lt;assignment_triggers&gt;
		&lt;unix&gt;
			active= no
			uim_user= pagerduty_unix
			service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/unix&gt;
		&lt;windows&gt;
			active= no
			uim_user= pagerduty_windows
			service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/windows&gt;
		&lt;vmware&gt;
			active= no
			uim_user= pagerduty_vmware
			service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/vmware&gt;
		&lt;storage&gt;
			active= no
			uim_user= pagerduty_storage
            service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/storage&gt;
		&lt;network&gt;
			active= no
			uim_user= pagerduty_network
			service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/network&gt;
		&lt;default&gt;
			active= yes
			uim_user= pagerduty_incident
			service_key= ##PAGERDUTY SERVICE KEY##
		&lt;/default&gt;
   &lt;/assignment_triggers&gt;
   
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class AssignmentTriggers implements Iterable<AssignmentTrigger> {

	private NDS data;

	public AssignmentTriggers(NDS nds) {
		this.data= nds;
	}
	
	
	@Override
	public Iterator<AssignmentTrigger> iterator() {
		
		
		final ArrayList<AssignmentTrigger> triggers= new ArrayList<AssignmentTrigger>();
		
		for (NDS t : data) {
			AssignmentTrigger trigger= new AssignmentTrigger(t);
			triggers.add(trigger);
		}
		
		return new Iterator<AssignmentTrigger>() {
			
			private ArrayList<AssignmentTrigger> items= triggers;
			private int i= 0;
			
			@Override
			public boolean hasNext() {
				return i < items.size();
			}
			
			@Override
			public AssignmentTrigger next() {
				return items.get(i++);
			}
			
			@Override
			public void remove() { }
		};
	}

	
	
	
	
	

}
