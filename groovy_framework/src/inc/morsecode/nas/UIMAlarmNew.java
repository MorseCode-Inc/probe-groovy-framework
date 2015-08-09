package inc.morsecode.nas;

import inc.morsecode.NDS;
import inc.morsecode.core.UIMMessage;

public class UIMAlarmNew extends UIMAlarmMessage {
	
	
	/**
	 *      "udata":{
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
	 * @param nds
	 */
	public UIMAlarmNew(NDS nds) { super(nds); }
	public UIMAlarmNew(UIMMessage message) { super(message); }
}
