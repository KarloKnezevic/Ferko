package hr.fer.zemris.jcms.web.interceptors.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

public class DelayedMessagesStore {
	private Logger logger = Logger.getLogger(DelayedMessagesStore.class);
	private Map<String, DelayedMessageEntry> map = new HashMap<String, DelayedMessageEntry>();
	private DelayedMessageEntry first;
	private DelayedMessageEntry last;
	private Random rand;
	private String base;
	
	public DelayedMessagesStore() {
		rand = new Random();
		base = Long.toHexString(rand.nextLong())+"G";
	}
	
	public synchronized IMessageContainer getAndRemove(String key) {
		DelayedMessageEntry entry = map.remove(key);
		if(entry==null) {
			return null;
		}
		if(first==entry) {
			first = entry.next;
		}
		if(last==entry) {
			last = entry.previous;
		}
		if(entry.next!=null) {
			entry.next.previous = entry.previous;
		}
		if(entry.previous!=null) {
			entry.previous.next = entry.next;
		}
		return entry.container;
	}

	public synchronized String put(IMessageContainer messages) {
		long now = System.currentTimeMillis();
		String key = base + rand.nextLong();
		DelayedMessageEntry entry = new DelayedMessageEntry();
		entry.container = messages;
		entry.expiresAt = now + 60*1000;
		entry.key = key;
		entry.next = null;
		entry.previous = null;
		if(last==null) {
			first = entry;
			last = entry;
		} else {
			last.next = entry;
			entry.previous = last;
			last = entry;
		}
		map.put(entry.key, entry);
		// Izbrisi sve koji su zaostali...
		DelayedMessageEntry e = first;
		while(e!=null && e.expiresAt<now) {
			map.remove(e.key);
			logger.warn("Removed forgotten delayed message with key="+e.key+".");
			e = e.next;
		}
		// Sada e pokazuje na prvog koji je trebao ostati...
		if(e!=null) {
			e.previous = null;
			first = e;
		} else {
			last = null;
			first = null;
		}
		return entry.key;
	}
	
	private class DelayedMessageEntry {
		private DelayedMessageEntry previous;
		private DelayedMessageEntry next;
		private String key;
		private long expiresAt;
		private IMessageContainer container;
	}
}
