package inc.morsecode.core;

import java.util.ArrayList;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimUserLogin;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.NimSubscribe;

import groovyjarjarantlr.debug.MessageListener;
import inc.morsecode.NDS;

public class QueueSubscription extends NDS {

	private transient NimSubscribe subscription;
	
	private ArrayList<UIMMessage> buffer;
	private ArrayList<MessageHandler> listeners;
	
	public QueueSubscription(String address, String clientName, String subject, int bulkSize) {
		super(clientName); // "subscription");
		set("address", address);
		set("name", clientName);
		set("subject", subject);
		set("queue", subject);
		set("bulk_size", Math.min(10000, Math.abs(bulkSize)));
		this.listeners= new ArrayList<MessageHandler>();
		this.buffer= new ArrayList<UIMMessage>();
	}
	
	public void register(MessageHandler listener) {
		listeners.add(listener);
	}
	
	public void unregister(MessageHandler listener) {
		listeners.remove(listener);
	}
	
	public void subscribe() throws NimException {
		// NimUserLogin.login("administrator", "this4now");
		String address= get("address", "no address specified");
		if (this.subscription == null || !this.subscription.isOk()) {
			if (subscription != null) { 
				this.subscription.close();
			}
			// this.subscription= new NimSubscribe("/UIM/MORSECODE", getName(), true);
			this.subscription= new NimSubscribe(getName(), address, true); // "/UIM/MORSECODE/_hub");
		}
		// this.subscription.subscribeForQueue(getQueueName(), this, "receive", getBulkSize());
		
		String queueName= getQueueName();
		
		if (getBulkSize() <= 1) {
			this.subscription.subscribeForQueue(queueName, this, "receive");
		} else {
			this.subscription.subscribeForQueue(queueName, this, "receive");
			// this.subscription.subscribeForQueue(queueName, this, "receive", getBulkSize());
		}
		
	}
	
	public void receive(NimSession session, PDS envelope, PDS args) throws NimException {
		try {
			NDS message= NDS.create(envelope);
			buffer.add(new UIMMessage(message));
		} finally {
			NDS reply= new NDS();
			session.sendReply(0, reply.toPDS());
			
		}
		
		UIMMessage msg= buffer.remove(0);
		for (MessageHandler listener : listeners) {
			try {
				listener.handle(msg);
			} catch (Throwable anything) {
				System.err.println("Handler Error: "+ anything);
			}
		}
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
