package inc.morsecode.pagerduty;

import inc.morsecode.NDS;
import inc.morsecode.nas.UIMAlarmNew;

public class AlarmDatabase {

	private NDS alarms= new NDS("alarms");

	
	public AlarmDatabase() {
		
		
	}
	
	
	public NDS getNDS() { return new NDS(alarms, false); }
	public String toString() { return alarms.toString(); }


	public void handle(UIMAlarmNew alarm) {
		System.out.println(alarm);
	}
}
