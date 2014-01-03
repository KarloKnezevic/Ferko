package hr.fer.zemris.jcms.locking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.locking.ILockManager;
import hr.fer.zemris.jcms.locking.LockPath;
import hr.fer.zemris.jcms.locking.UnlockException;

public class InProcessLockManager implements ILockManager {
	
	private LockTreeNode root = new LockTreeNode();

	public InProcessLockManager() {
		root.children = new HashMap<String, LockTreeNode>();
	}
	
	@Override
	public synchronized void acquireLock(LockPath lockPath) {
		System.out.println("[LockManager].acquire("+lockPath+", "+Thread.currentThread().getName()+")");
		// Ako lista nije prazna, tada
		boolean inList = false;
		int size = lockPath.size();
		while(true) {
			LockTreeNode current = root;
			boolean shouldWait = false;
			for(int i=0; i < size; i++) {
				// Ako je ovo zadnji korak
				if(i==size-1) {
					if(shouldWait) continue;
					if(current.locked) { // Ako je cvor zakljucan
						shouldWait = true;
					} else { // Ako nije zakljucan
						// Ako sam ja u listi
						if(inList) {
							// Ima li čvor djece? Ako ima, netko niže drži lock pa ja ne smijem!!!
							if(current.lockCounter>0) {
								shouldWait = true;
							} else {
								// je li dosao red na mene? Ako je:
								if(current.priorityList.peek()==Thread.currentThread()) {
									// makni me iz reda, ja sam na redu za uzeti lock
									current.priorityList.remove();
								} else {
									// ja nisam na redu
									shouldWait = true;
								}
							}
						} else { // ako nisam u redu cekanja (a cvor nije zakljucan)
							// ako netko drugi vec ceka, moram i ja cekati...
							if(current.priorityList!=null && !current.priorityList.isEmpty()) {
								shouldWait = true;
							} else if(current.lockCounter>0) {
								shouldWait = true;
							}
						}
					}
					if(shouldWait) {
						if(!inList) {
							if(current.priorityList==null) {
								current.priorityList = new LinkedList<Thread>();
							}
							current.priorityList.add(Thread.currentThread());
							inList = true;
						}
					}
				} else { // inače
					if(current.locked || current.priorityList!=null && !current.priorityList.isEmpty()) {
						shouldWait = true;
					}
					// Kako se zove dijete?
					String childName = lockPath.getPart(i+1);
					if(current.children==null) {
						current.children = new HashMap<String, LockTreeNode>();
					}
					LockTreeNode next = current.children.get(childName);
					if(next==null) {
						next = new LockTreeNode();
						next.name = childName;
						current.children.put(childName, next);
						next.parent = current;
					}
					current = next;
				}
			}
			// current pokazuje na moj aktualni čvor
			if(shouldWait) {
				try {
					this.wait(2000);
				} catch (InterruptedException ignorable) {
				}
				continue;
			} else {
				current.owner = Thread.currentThread();
				current.locked = true;
				while(current != null) {
					current.lockCounter++;
					current = current.parent;
				}
				System.out.println("[LockManager].acquire("+lockPath+", "+Thread.currentThread().getName()+") granted.");
				break;
			}
		}
	}
	
	@Override
	public synchronized void releaseLock(LockPath lockPath) throws UnlockException {
		int size = lockPath.size();
		LockTreeNode current = root;
		// Pronađi zaključani list
		for(int i=0; i < size; i++) {
			// Ako je ovo zadnji korak
			if(i==size-1) {
				if(!current.locked) {
					throw new UnlockException("Path "+lockPath+" is not locked!");
				}
				if(current.owner!=Thread.currentThread()) {
					throw new UnlockException("Path "+lockPath+" is not locked by you! Locker is "+current.owner.toString());
				}
			} else { // inače
				// Kako se zove dijete?
				String childName = lockPath.getPart(i+1);
				LockTreeNode next = null;
				if(current.children!=null) {
					next = current.children.get(childName);
				}
				if(next==null) {
					throw new UnlockException("Path "+lockPath+" does not exists so it can not be unlocked!");
				}
				current = next;
			}
		}
		current.locked = false;
		current.owner = null;
		LockTreeNode n = current;
		while(n!=null) {
			n.lockCounter--;
			n = n.parent;
		}
		while(true) {
			if(current.locked) {
				// Ajme meni! Ovo ne smije biti moguće! Nakon ovoga stvari više ne rade.
				throw new UnlockException("Problem in cleaning "+lockPath+". Path "+current.toString()+" was also found locked.");
			}
			// Ako čvor ima prioritetnu listu, gotov sam s čišćenjem
			if(current.priorityList!=null && !current.priorityList.isEmpty()) {
				break;
			}
			// Ako čvor ima djecu, gotov sam s čišćenjem
			if(current.children!=null && !current.children.isEmpty()) {
				break;
			}
			// Inače ovaj čvor nema niti djece, niti čekača; obriši ga ako ima roditelja
			if(current.parent!=null) {
				current.parent.children.remove(current.name);
				current = current.parent;
				continue;
			} else {
				// Inače je ovo vršni čvor; njega ne brišem i gotov sam.
				break;
			}
		}
		System.out.println("[LockManager].unlocked("+lockPath+", "+Thread.currentThread().getName()+")");
		this.notifyAll();
	}
	
	private static class LockTreeNode {
		String name;
		Thread owner;
		boolean locked;
		LockTreeNode parent;
		private LinkedList<Thread> priorityList;
		Map<String, LockTreeNode> children;
		int lockCounter = 0;
		
		@Override
		public String toString() {
			List<String> parts = new ArrayList<String>();
			LockTreeNode current = this;
			while(current!=null) {
				parts.add(current.name);
				current = current.parent;
			}
			StringBuilder sb = new StringBuilder(parts.size()*10);
			sb.append(parts.get(0));
			for(int i = 1; i < parts.size(); i++) {
				sb.append('/').append(parts.get(i));
			}
			return sb.toString();
		}
	}
	
	public static void main(String[] args) {
		// Embeded test
		final ILockManager man = new InProcessLockManager();
		final long now = System.currentTimeMillis();
		
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(10); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci17\\g\\g7");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 1: ml\\ci17\\g\\g7").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(10); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci17\\g\\g8");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 2: ml\\ci17\\g\\g8").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(10); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci18\\g\\g8");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 3: ml\\ci18\\g\\g8").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(4000); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci17\\g");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 4: ml\\ci17\\g").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(5000); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 5: ml").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(4000); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci17");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 6: ml\\ci17\\g").start();
		new Thread(new Runnable() {public void run() {
			try { Thread.sleep(4000); } catch(Exception ex) {}
			try {
				System.out.println(Thread.currentThread().getName()+": čekam lock"+time(now));
				LockPath p = new LockPath("ml\\ci17\\g");
				try { man.acquireLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
				System.out.println(Thread.currentThread().getName()+": radim"+time(now));
				try { Thread.sleep(10000); } catch(Exception ex) {}
				System.out.println(Thread.currentThread().getName()+": oslobađam lock"+time(now));
				try { man.releaseLock(p); } catch(Exception ex) { System.out.println(ex.getMessage()); }
			} catch(Exception ex) {ex.printStackTrace();}
		}}, "Radnik 7: ml\\ci17\\g").start();
		
	}

	protected static String time(long now) {
		double d = (System.currentTimeMillis()-now)/1000.0;
		return " "+(int)(d+0.5);
	}
}
