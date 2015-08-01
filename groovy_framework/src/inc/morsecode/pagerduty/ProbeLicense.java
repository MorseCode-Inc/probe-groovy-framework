package inc.morsecode.pagerduty;

import inc.morsecode.NDS;

import com.nimsoft.nimbus.NimException;

import util.kits.SimpleCalendar;

public class ProbeLicense extends NDS {
	
	private String issuedTo;
	private SimpleCalendar validThru;
	private int instances;

	public ProbeLicense(String issuedTo, int instances, SimpleCalendar validThru) {
		setName("pagerduty_license");
		set("Issued To", issuedTo);
		set("Instances", instances);
		set("Valid Until", validThru.toString());
	}
	
	
	final public void setKey(String key) { set("key", key); }
	final public String getKey() { return get("key", null); }
	
	final public int getInstances() {
		return get("Instances", 1);
	}
	
	final public String getIssuedTo() {
		return get("Issued To", "Demo");
	}
	
	final public String getValidUntil() {
		return get("Valid Until", "1970-01-01 00:00");
	}
	
	/*
	public String toString() {
		String str= "";
		String nl= "\n";
		
		str+= "Issued To: "+ issuedTo;
		str+= nl+ "Licensed Instances: "+ instances;
		str+= nl+ "Valid Thru: "+ validThru;
		
		if (key != null) {
			str+= nl +"Key: "+ key;
		}
		
		return str;
	}
	*/
	
	
	public static void main(String[] args) throws NimException {
		
		SimpleCalendar expires= new SimpleCalendar();
		expires.setDay(1);
		expires.zeroTime();
		
		for (int i= 1; i < 24; i++) {
			
			expires.advanceMonth(1);
		
			ProbeLicense l= new ProbeLicense("Demo", 2, expires);
			
			HttpGateway.create(l);
		
			System.out.println(l.getValidUntil() +" "+ l.getKey());
		
		}
		
	}


}
