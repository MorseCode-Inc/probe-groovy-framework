package inc.morsecode.pagerduty;

import inc.morsecode.NDS;
import inc.morsecode.nas.UIMAlarmAssign;
import inc.morsecode.nas.UIMAlarmClose;
import inc.morsecode.nas.UIMAlarmNew;
import inc.morsecode.nas.UIMAlarmUnassign;
import inc.morsecode.nas.UIMAlarmUpdate;
import inc.morsecode.pagerduty.api.PDClient;

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

	private PDClient client;
	
	public AlarmManager(PDClient client) {
		this.client= client.newInstance();
		
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
		
		System.out.println("NEW Alarm "+ alarm.getAlarmSeverity().toUpperCase() +": "+ alarm.getAlarmMessage());
	}
	
	
	public void handle(UIMAlarmUpdate alarm) {
		
		System.out.println("UPDATE Alarm "+ alarm.getAlarmSeverity().toUpperCase() +": "+ alarm.getAlarmMessage());
		System.out.println("subject: "+ alarm.getSubject());
		// System.out.println("body: \n"+ alarm.getBody());
		System.out.println("message: \n"+ alarm.toJson());
		System.out.println();
	}
	
	public void handle(UIMAlarmClose message) {
		
		System.out.println("subject: "+ message.getSubject());
		System.out.println("body: \n"+ message.getBody());
		System.out.println("message: \n"+ message.toJson());
		System.out.println();
	}
	
	public void handle(UIMAlarmAssign message) {
		
		System.out.println("subject: "+ message.getSubject());
		System.out.println("body: \n"+ message.getBody());
		System.out.println("message: \n"+ message.toJson());
		System.out.println();
	}
	
	public void handle(UIMAlarmUnassign message) {
		
		System.out.println("subject: "+ message.getSubject());
		System.out.println("body: \n"+ message.getBody());
		System.out.println("message: \n"+ message.toJson());
		System.out.println();
	}
}
