package inc.morsecode.pagerduty;

import java.io.IOException;

import util.json.JsonArray;
import util.json.ex.MalformedJsonException;
import inc.morsecode.NDS;
import inc.morsecode.nas.UIMAlarmAssign;
import inc.morsecode.nas.UIMAlarmClose;
import inc.morsecode.nas.UIMAlarmNew;
import inc.morsecode.nas.UIMAlarmUnassign;
import inc.morsecode.nas.UIMAlarmUpdate;
import inc.morsecode.pagerduty.api.IncidentUpdateParameters;
import inc.morsecode.pagerduty.api.PDClient;
import inc.morsecode.pagerduty.api.PagerDutyIncidentsAPI;
import inc.morsecode.pagerduty.data.PDService;
import inc.morsecode.pagerduty.data.PDTriggerEvent;

/**
 * 
 * &copy; MorseCode Incorporated 2015
 * =--------------------------------=<br/><pre>
 * Created: Aug 8, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class AlarmManager {

	private NDS alarms= new NDS("alarms");
	//private Probe probe;

	private ServiceMapper smap;
	
	private PDClient client;
	
	private AssignmentTriggers assignmentTriggers;
	private AlarmTriggersConfig alarmTriggers;
	
	public AlarmManager(AlarmTriggersConfig alarmTriggers, AssignmentTriggers assignmentTriggers, PDClient client, ServiceMapper smap) {
		this.client= client.newInstance();
		this.smap= smap;
		this.alarmTriggers= alarmTriggers;
		this.assignmentTriggers= assignmentTriggers;
	}


	public NDS getNDS() { return new NDS(alarms, false); }
	public String toString() { return alarms.toString(); }


	/**
	 * 
	 * <pre>
	 * {
     *        "nimts":1439082048
     *        , "tz_offset":14400
     *        , "subject":"alarm_new"
     *        , "hop0":"MORSECODE"
     *        , "origin":"MORSECODE"
     *        , "prid":"nas"
     *        , "robot":"_hub"
     *        , "pri":1
     *        , "qsize":1
     *        , "source":"162.248.167.44"
     *        , "domain":"UIM"
     *        , "nimid":"QY00588503-62941"
     *        , "hop":0
     *        , "udata":{
     *                "sid":"3.6526.3"
     *                , "nimts":1439082027
     *                , "visible":1
     *                , "tz_offset":14400
     *                , "dev_id":"D52248754FCF4FDAE7378AB198CD0EDA0"
     *                , "event_type":1
     *                , "origin":"CLOUD"
     *                , "subsys":"3.6526.3"
     *                , "hostname":"support.morsecode-inc.com"
     *                , "prid":"http_gateway"
     *                , "robot":"outpost"
     *                , "severity":"warning"
     *                , "hub":"MORSECODE"
     *                , "message":"test"
     *                , "level":2
     *                , "met_id":"M0097209ccf5d59ff9c61778d897bd4ab"
     *                , "supp_key":"alarm/manual1"
     *                , "source":"outpost"
     *                , "domain":"UIM"
     *                , "nimid":"QY00588503-62938"
     *                , "suppcount":0
     *                , "arrival":1439082043
     *                , "nas":"MORSECODE"
     *                , "rowid":1
     *         }
     * }
     *
	 * </pre>
	 * 
	 * @param alarm
	 */
	public void handle(UIMAlarmNew alarm) {
		System.out.println("ALARM [alarm:"+ alarm.getNimid() +"] "+ alarm.getAlarmSeverity().toUpperCase() +" "+ alarm.getAlarmSource() +"("+ alarm.getAlarmHostname() +")" +": "+ alarm.getAlarmMessage());
		
		PDTriggerEvent event= smap.map(alarm);
		
		String serviceKey= event.getServiceKey();
		
		// PagerDutyIncidentsAPI.buildPdTrigger(serviceKey, alarm, new JsonArray());
		
// for now limit what is sent to PD
if (!"outpost".equals(alarm.getAlarmRobot())) { return; }
if (!"httpgtw".equals(alarm.getAlarmPrid())) { return; }
		
		try {
			client.incidents().send(event);
		} catch (IOException e) {
			System.err.println("Error sending PagerDuty Event Trigger: "+ event);
			e.printStackTrace();
		} catch (MalformedJsonException e) {
			System.err.println("Error sending PagerDuty Event Trigger: "+ event);
			e.printStackTrace();
		}
	}
	
	
	public void handle(UIMAlarmUpdate alarm) {
		
		System.out.println("UPDATE [alarm:"+ alarm.getNimid() +"] "+ alarm.getAlarmSeverity().toUpperCase() +" "+ alarm.getAlarmSource() +"("+ alarm.getAlarmHostname() +")" +": "+ alarm.getAlarmMessage());
		// System.out.println("subject: "+ alarm.getSubject());
		// System.out.println("body: \n"+ alarm.getBody());
		// System.out.println("message: \n"+ alarm.toJson());
		// System.out.println();
		

		PagerDutyIncidentsAPI incidents= new PagerDutyIncidentsAPI(client);
		
		IncidentUpdateParameters params= new IncidentUpdateParameters();
		
		// incidents.update(user.getId(), params);
		// System.out.println(urls.getEventTrigger(client, event));
		
	}
	
	public void handle(UIMAlarmClose alarm) {
		
		System.out.println("CLOSE [alarm:"+ alarm.getNimid() +"] "+ alarm);
		//System.out.println("subject: "+ message.getSubject());
		//System.out.println("body: \n"+ message.getBody());
		//System.out.println("message: \n"+ message.toJson());
		//System.out.println();
	}
	
	public void handle(UIMAlarmAssign alarm) {
		
		System.out.println("ASSIGN [alarm:"+ alarm.getNimid() +"] "+ alarm);
		
		String assignedToUimUser= alarm.getAlarmAssignedTo();
		
		for (AssignmentTrigger config : assignmentTriggers) {
			if (!config.isActive()) { continue; }
			String username= config.getUimUsername(null);
			
			if (username == null) {
				System.err.println("Missing required username for alarm trigger configuration: "+ config.getName());
				continue;
			}
			
			String defaultServiceKey= null; // TODO: FIGURE OUT THIS
			String serviceKey= config.getServiceKey(defaultServiceKey);
			
			if (serviceKey == null || "".equals(serviceKey)) {
				System.err.println("Missing required service_key for alarm trigger configuration: "+ config.getName());
				continue;
			}
			
			if (username.equals(assignedToUimUser)) {
				
				
				PDService service= smap.get(serviceKey);
				
				PDTriggerEvent event= new PDTriggerEvent(service, alarm);
				
				
				client.incidents().send(event);
				
				
				
			}
			
		}
		
		// System.out.println("subject: "+ message.getSubject());
		// System.out.println("body: \n"+ message.getBody());
		System.out.println("message: \n"+ alarm.toJson());
		//System.out.println();
	}
	
	public void handle(UIMAlarmUnassign alarm) {
		
		System.out.println("UNASSIGN [alarm:"+ alarm.getNimid() +"] "+ alarm);
		// System.out.println("subject: "+ message.getSubject());
		// System.out.println("body: \n"+ message.getBody());
		// System.out.println("message: \n"+ message.toJson());
		//System.out.println();
	}
}
