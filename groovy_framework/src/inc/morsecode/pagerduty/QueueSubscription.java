package inc.morsecode.pagerduty;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimUserLogin;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.NimSubscribe;

import inc.morsecode.NDS;

public class QueueSubscription extends NDS {

	private transient NimSubscribe subscription;
	
	public QueueSubscription(String clientName, String subject, int bulkSize) {
		super(clientName); // "subscription");
		set("name", clientName);
		set("subject", subject);
		set("bulk_size", Math.min(10000, Math.abs(bulkSize)));
	}
	
	public void subscribe() throws NimException {
		NimUserLogin.login("administrator", "this4now");
		if (this.subscription == null || !this.subscription.isOk()) {
			// this.subscription= new NimSubscribe("/UIM/MORSECODE", getName(), true);
			this.subscription= new NimSubscribe("test");
		}
		// this.subscription.subscribeForQueue(getQueueName(), this, "receive", getBulkSize());
		
		this.subscription.subscribeForQueue(getQueueName(), this, "receive");
		
	}
	
	public void receive(NimSession session, PDS envelope, PDS args) throws NimException {
		NDS message= NDS.create(envelope);
		System.out.println(message);
		NDS reply= new NDS();
		session.sendReply(0, reply.toPDS());
	}
	
	/*
	public void receive(NimSession session, PDS[] envelopes, PDS[] args) throws NimException {
		for (int i= 0; i < envelopes.length; i++) {
			
			NDS message= NDS.create(envelopes[i]);
			System.out.println(message);
		}
		NDS reply= new NDS();
		session.sendReply(0, reply.toPDS());
	}
	*/
	
	
	public String getQueueName() { return this.get("queue", null); }
	
	public int getBulkSize() { return Math.min(10000, Math.abs(this.get("bulk_size", 1))); }

	public boolean isOk() {
		if (this.subscription == null) {
			return false;
		}
		
		return this.subscription.isOk();
	}
}
