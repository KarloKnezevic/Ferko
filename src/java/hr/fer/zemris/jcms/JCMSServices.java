package hr.fer.zemris.jcms;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import hr.fer.zemris.jcms.activities.IActivityWorker;
import hr.fer.zemris.jcms.bcon.BConServer;
import hr.fer.zemris.jcms.bcon.impl.BConServerImpl;
import hr.fer.zemris.jcms.periodicals.PeriodicalServiceDescriptor;

public class JCMSServices {

	private static final Logger logger = Logger.getLogger(JCMSServices.class.getCanonicalName());

	private static BConServer bConServer;
	private static volatile boolean periodicalThreadStop = false;
	private static Thread periodicalThread;
	
	public static void start() {
		bConServer = new BConServerImpl(null);
		bConServer.start();
		long now = new Date().getTime();
		for(PeriodicalServiceDescriptor d : JCMSSettings.getSettings().periodicalServices) {
			d.setNextActivationAt(periodicalsCalcNext(d, now));
			d.getService().init();
		}
		periodicalThreadStop = false;
		periodicalThread = new Thread(new Runnable() {
			@Override
			public void run() {
				periodicals();
			}
		});
		periodicalThread.start();
		IActivityWorker actWorker = (IActivityWorker)JCMSSettings.getSettings().getObjects().get("activityWorker");
		actWorker.start();
	}

	public static boolean periodicalPassMessage(String name, final String key, final String value) {
		PeriodicalServiceDescriptor[] pds = JCMSSettings.getSettings().periodicalServices;
		Thread t = null;
		for(int i = 0; i < pds.length; i++) {
			final PeriodicalServiceDescriptor d = pds[i];
			if(d.getClassName().equals(name)) {
				t = new Thread(new Runnable() {
					@Override
					public void run() {
						d.getService().passMessage(key, value);
					}
				});
				break;
			}
		}
		if(t==null) return false;
		t.start();
		return true;
	}
	
	protected static void periodicals() {
		logger.info("Periodicals watcher started.");
		while(true) {
			try { Thread.sleep(1000*60); } catch(Exception ex) {}
			if(periodicalThreadStop) break;
			long now = new Date().getTime();
			PeriodicalServiceDescriptor[] pds = JCMSSettings.getSettings().periodicalServices;
			for(int i = 0; i < pds.length; i++) {
				final PeriodicalServiceDescriptor d = pds[i];
				synchronized(d) {
					if(!d.isInCall() && d.getNextActivationAt()<=now) {
						d.setInCall(true);
						d.setLastActivatedOn(now);
						d.setNextActivationAt(periodicalsCalcNext(d, now));
						new Thread(new Runnable() {
							@Override
							public void run() {
								logger.info("Starting periodical service: "+d.getClassName());
								try {
									d.getService().periodicalExecute();
								} catch(Throwable t) {
									logger.error("Exception while running periodical service: "+d.getClassName(), t);
								}
								synchronized(d) {
									d.setInCall(false);
								}
							}
						}).start();
					}
				}
			}
		}
		logger.info("Periodicals watcher ended.");
	}

	private static long periodicalsCalcNext(PeriodicalServiceDescriptor d, long now) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(now));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long dayStart = c.getTime().getTime();
		long delta = now - dayStart;
		long[] callPoints = d.getCallPoints(); 
		for(int i = 0; i < callPoints.length; i++) {
			if(callPoints[i] > delta) {
				return dayStart+callPoints[i];
			}
		}
		return dayStart + 24L*60L*60L*1000L + callPoints[0];
	}

	public static void stop() {
		IActivityWorker actWorker = (IActivityWorker)JCMSSettings.getSettings().getObjects().get("activityWorker");
		actWorker.stop();
		periodicalThreadStop = true;
		periodicalThread.interrupt();
		for(PeriodicalServiceDescriptor d : JCMSSettings.getSettings().periodicalServices) {
			d.getService().destroy();
		}
		if(bConServer != null) bConServer.stop();
	}
	
}
