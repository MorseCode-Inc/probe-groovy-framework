package inc.morsecode.nas;

import inc.morsecode.NDS;
import inc.morsecode.core.UIMMessage;

public class UIMAlarmMessage extends UIMMessage {

	public static final String SID = "sid";
	public static final String NIMTS = "nimts";
	public static final String VISIBLE = "visible";
	public static final String TZ_OFFSET = "tz_offset";
	public static final String DEV_ID = "dev_id";
	public static final String EVENT_TYPE = "event_type";
	public static final String ORIGIN = "origin";
	public static final String SUBSYS = "subsys";
	public static final String HOSTNAME = "hostname";
	public static final String PRID = "prid";
	public static final String ROBOT = "robot";
	public static final String SEVERITY = "severity";
	public static final String HUB = "hub";
	public static final String MESSAGE = "message";
	public static final String LEVEL = "level";
	public static final String MET_ID = "met_id";
	public static final String SUPP_KEY = "supp_key";
	public static final String SOURCE = "source";
	public static final String DOMAIN = "domain";
	public static final String NIMID = "nimid";
	public static final String SUPPCOUNT = "suppcount";
	public static final String ARRIVAL = "arrival";
	public static final String NAS = "nas";
	public static final String ROWID = "rowid";
	
	public static final String ASSIGNED_TO = "assigned_to";
	public static final String ASSIGNED_BY = "assigned_by";
	public static final String ASSIGNED_AT = "assigned_at";
	
	public static final String OSUSER1 = "user_tag1";
	public static final String OSUSER2 = "user_tag2";
	
	public static final String CUSTOM1 = "custom_1";
	public static final String CUSTOM2 = "custom_2";
	public static final String CUSTOM3 = "custom_3";
	public static final String CUSTOM4 = "custom_4";
	public static final String CUSTOM5 = "custom_5";

	public UIMAlarmMessage(NDS nds) {
		super(nds);
	}

	public String getAlarmSid() { return get("udata/"+ SID); }
	
	public String getAlarmAssignedTo() { return get("udata/"+ ASSIGNED_TO); }
	public String getAlarmAssignedAt() { return get("udata/"+ ASSIGNED_AT); }
	public String getAlarmAssignedBy() { return get("udata/"+ ASSIGNED_BY); }

	public String getAlarmNimts() { return get("udata/"+ NIMTS); }

	public String getAlarmVisible() { return get("udata/"+ VISIBLE); }

	public String getAlarmTzOffset() { return get("udata/"+ TZ_OFFSET); }

	public String getAlarmDevId() { return get("udata/"+ DEV_ID); }

	public String getAlarmEventType() { return get("udata/"+ EVENT_TYPE); }

	public String getAlarmOrigin() { return get("udata/"+ ORIGIN); }

	public String getAlarmSubsys() { return get("udata/"+ SUBSYS); }

	public String getAlarmHostname() { return get("udata/"+ HOSTNAME); }

	public String getAlarmPrid() { return get("udata/"+ PRID); }

	public String getAlarmRobot() { return get("udata/"+ ROBOT); }

	public String getAlarmSeverity() { return get("udata/"+ SEVERITY); }

	public String getAlarmHub() { return get("udata/"+ HUB); }

	public String getAlarmMessage() { return get("udata/"+ MESSAGE); }

	public String getAlarmLevel() { return get("udata/"+ LEVEL); }

	public String getAlarmMetId() { return get("udata/"+ MET_ID); }

	public String getAlarmSuppKey() { return get("udata/"+ SUPP_KEY); }

	public String getAlarmSource() { return get("udata/"+ SOURCE); }

	public String getAlarmDomain() { return get("udata/"+ DOMAIN); }

	public String getAlarmNimid() { return get("udata/"+ NIMID); }

	public String getAlarmSuppCount() { return get("udata/"+ SUPPCOUNT); }

	public String getAlarmArrival() { return get("udata/"+ ARRIVAL); }

	public String getAlarmNas() { return get("udata/"+ NAS); }

	public String getRowId() { return get("udata/"+ ROWID); }
	
	public String getUserTag1() { return get("udata/"+ OSUSER1); }
	public String getUserTag2() { return get("udata/"+ OSUSER2); }
	
	public String getCustom1() { return get("udata/"+ CUSTOM1); }
	public String getCustom2() { return get("udata/"+ CUSTOM2); }
	public String getCustom3() { return get("udata/"+ CUSTOM3); }
	public String getCustom4() { return get("udata/"+ CUSTOM4); }
	public String getCustom5() { return get("udata/"+ CUSTOM5); }


}