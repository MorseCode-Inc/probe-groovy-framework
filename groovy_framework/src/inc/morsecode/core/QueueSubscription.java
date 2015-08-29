package inc.morsecode.core;

import java.util.ArrayList;
import java.util.Vector;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimSession;
import com.nimsoft.nimbus.NimUserLogin;
import com.nimsoft.nimbus.PDS;
import com.nimsoft.nimbus.NimSubscribe;

import inc.morsecode.NDS;
import inc.morsecode.etc.Mutex;

public class QueueSubscription extends NDS implements Runnable {
	
	private Mutex lock= new Mutex();

	private transient NimSubscribe subscription;
	
	private Vector<UIMMessage> buffer;
	private Vector<MessageHandler> listeners;
	private Thread thread;
	
	private boolean ready= false;
	
	public QueueSubscription(String address, String clientName, String queue, int bulkSize) {
		super(clientName); // "subscription");
		set("address", address);
		set("name", clientName);
		set("queue", queue);
		set("bulk_size", Math.min(10000, Math.abs(bulkSize)));
		this.listeners= new Vector<MessageHandler>();
		this.buffer= new Vector<UIMMessage>();
		this.ready= true;
		thread= new Thread(this);
		thread.start();
	}
	
	public void register(MessageHandler listener) {
		while (!lock()) {
			try { Thread.sleep(33); } catch (InterruptedException ignored) { } 
		}
		if (!listeners.contains(listener)) { 
			listeners.add(listener);
		}
		release();
	}
	
	public void unregister(MessageHandler listener) {
		while (!lock()) { try { Thread.sleep(33); } catch (InterruptedException ignored) { }  }
		listeners.remove(listener);
		release();
	}
	
	public void subscribe() throws NimException {
		try {
			if (!lock()) { return; }
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
		
		if (subscription.isOk()) { 
			return;
		}
		
		if (getBulkSize() <= 1) {
			this.subscription.subscribeForQueue(queueName, this, "receive");
		} else {
			this.subscription.subscribeForQueue(queueName, this, "receive");
			// this.subscription.subscribeForQueue(queueName, this, "receive", getBulkSize());
		}
		} finally {
			lock.release();
		}
		
	}
	
	public void run() { 
		while (ready) {
			
			try { Thread.sleep(3000);  } catch (InterruptedException ignore) { }
			
			
			try {
				while (!lock()) { try { Thread.sleep(2); } catch (InterruptedException ignored) { }  }
				
				while (!buffer.isEmpty()) {
					UIMMessage msg= buffer.remove(0);
					MessageHandler[] handlers= listeners.toArray(new MessageHandler[]{});
				
					for (MessageHandler listener : handlers) {
						try {
							listener.handle(msg);
						} catch (Throwable anything) {
							System.err.println("Handler Error: "+ anything);
							anything.printStackTrace();
						}
					}
				}
				} finally { 
				release();
			}
		}
	}
	
	public void receive(NimSession session, PDS envelope, PDS args) throws NimException {
		System.out.println("["+ envelope.get("subject") +":"+ envelope.get("nimid") +"] from "+ envelope.get("robot") +"("+ envelope.get("prid") +")");
		try {
			while (!lock()) { try { Thread.sleep(2); } catch (InterruptedException ignored) { }  }
			try {
				NDS message= NDS.create("message", envelope);
				buffer.add(new UIMMessage(message));
			} finally {
			}
			

		} finally {
			NDS reply= new NDS();
			session.sendReply(0, reply.toPDS());
			release();
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
	
	public void stop() {
		if (subscription != null) {
			subscription.close();
		}
		ready= false;
	}
	
	private boolean lock() {
		if (this.lock == null) { this.lock= new Mutex(); }
		return this.lock.lock(1);
	}
	
	
	private void release() {
		if (this.lock == null) { this.lock= new Mutex(); }
		this.lock.release();
	}
}
