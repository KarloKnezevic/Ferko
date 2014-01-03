package hr.fer.zemris.jcms.service.locks;

import java.util.HashMap;
import java.util.Map;

public class MPLockFactory {
	
	private static final MPLock lock = new MPLockImpl();
	
	public static MPLock get() {
		return lock;
	}
	
	private static class MPLockImpl implements MPLock {

		private Map<Long,LockEntry> locks = new HashMap<Long, LockEntry>(128);
		
		@Override
		public void releaseLock(Long marketPlaceGroupID) {
			if(marketPlaceGroupID==null) return;
			LockEntry en = null;
			synchronized (locks) {
				en = locks.get(marketPlaceGroupID);
				if(en==null) {
					// Ovo je neka grozna greska!!!
					return;
				}
				en.counter--;
				if(en.counter<1) {
					locks.remove(marketPlaceGroupID);
				}
			}
			synchronized (en) {
				en.locked = false;
				en.notifyAll();
			}
		}

		@Override
		public void writeLock(Long marketPlaceGroupID) {
			if(marketPlaceGroupID==null) return;
			LockEntry en = null;
			synchronized (locks) {
				en = locks.get(marketPlaceGroupID);
				if(en==null) {
					en = new LockEntry();
					locks.put(marketPlaceGroupID, en);
				}
				en.counter++;
			}
			synchronized (en) {
				while(en.locked) {
					try { en.wait(); } catch(Exception ex) {}
				}
				en.locked = true;
			}
		}
		
		private class LockEntry {
			int counter = 0;
			boolean locked = false;
		}
	}

}
