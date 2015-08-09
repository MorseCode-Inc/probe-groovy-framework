package inc.morsecode.core;

import inc.morsecode.NDS;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 8, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * </pre></br>
 * =--------------------------------=
 * 
 */
public class UIMMessage extends NDS {
	
	public static final String NIMTS= "nimts";
	
	public static final String TZ_OFFSET= "tz_offset"; // :14400
	public static final String SUBJECT= "subject"; // :"alarm_new"
	public static final String ORIGIN= "origin"; // :"MORSECODE"
	public static final String PRID= "prid"; // :"nas"
	public static final String ROBOT= "robot"; // :"_hub"
	public static final String PRI= "pri"; // :1
	public static final String QSIZE= "qsize"; // :1
	public static final String SOURCE= "source";
	public static final String DOMAIN= "domain"; // :"UIM"
	public static final String NIMID= "nimid"; // :"QY00588503-62941"
	public static final String HOP= "hop"; // :0
	public static final String UDATA= "udata"; // : {...}

	/**
	 * <pre>
	 * {
	 *  	 "nimts":1439082048
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
     *        , "udata": {...}
     *  }
     *  </pre>
	 * @param nds
	 */
	public UIMMessage(NDS nds) {
		super(nds);
		if (getName() == null) {
			setName("message");
		}
	}
	
	public String getSubject() { return get("subject"); }
	
	public NDS getBody() { return seek("udata"); }
	
	
	public String getNimid() { return get(NIMID); }
	public String getNimts() { return get(NIMTS); }
	public String getTzOffset() { return get(TZ_OFFSET); }
	public String getOrigin() { return get(ORIGIN); }
	public String getSource() { return get(SOURCE); }
	public String getRobot() { return get(ROBOT); }
	public String getProbeName() { return get(PRID); }
	public String getPriority() { return get(PRI); }
	public String getQueueSize() { return get(QSIZE); }
	public String getDomain() { return get(DOMAIN); }
	public int getHops() { return get(HOP, 0); }
	public String getHop(int id) { return get(HOP + Math.abs(id)); }
	
}
