package inc.morsecode.pagerduty;

import java.util.List;

import inc.morsecode.NDS;
import inc.morsecode.nas.UIMAlarmMessage;
import inc.morsecode.pagerduty.api.PDService;
import inc.morsecode.pagerduty.api.PDTriggerEvent;

public class ServiceMapper {
	
	private List<PDService> services;
	private NDS mapping;

	public ServiceMapper(List<PDService> services, NDS mapping) {
		this.services= services;
		this.mapping= mapping;
	}
	
	public PDTriggerEvent map(UIMAlarmMessage alarm) {
		PDService service= services.get(0);
		PDTriggerEvent trigger= new PDTriggerEvent(service, alarm);
		
		
		return trigger;
	}
	
	public boolean matches(UIMAlarmMessage alarm, AlarmServiceFilter filter) {
		
		return true;
		
	}

}