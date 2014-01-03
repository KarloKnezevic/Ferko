package hr.fer.zemris.jcms;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class JCMSLogger {

	private static final Logger logger = Logger.getLogger(JCMSLogger.class.getCanonicalName());
	
	private static JCMSLogger jlogger = new JCMSLogger();
	private Writer w;
	private SimpleDateFormat sdf;
	private Thread thread;
	private boolean dirty;
	private volatile boolean stopRequested;
	
	private JCMSLogger() {
		try {
			w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(JCMSSettings.getSettings().getMpLogsDir(),"mp.log"),true)), "UTF-8");
		} catch(Exception ex) {
			logger.error("Could not create mp logger file.", ex);
		}
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					kick();
				} finally {
					
				}
			}
		});
		dirty = false;
		thread.start();
	}
	
	protected void kick() {
		if(w==null) return;
		synchronized(w) {
			while(true) {
				// Ako netko ocisti w
				if(w==null) break;
				// Inace cekaj 10 sekundi
				try { w.wait(10000); } catch(InterruptedException ignorable) {}
				// I ako treba, flush-aj log 
				try { if(dirty) w.flush(); } 
				catch(IOException ignorable) {} 
				finally { dirty = false; }
				// Ako nas pokusavaju zaustaviti
				if(stopRequested) break;
			}
			// OK, zatvori w
			try { if(w!=null) w.close(); } catch(IOException ignorable) {}
		}
	}

	public static JCMSLogger getLogger() {
		return jlogger;
	}

	public static void stop() {
		if(jlogger.w!=null) {
			synchronized(jlogger.w) {
				jlogger.stopRequested = true;
				jlogger.w.notifyAll();
			}
		}
	}
	
	public void mpLogSwitch(MarketPlace marketPlace, User firstUser, Group firstGroup, User secondUser, Group secondGroup, String kind) {
		if(w==null) return;
		Date now = new Date();
		synchronized(w) {
			StringBuilder sb = new StringBuilder(256);
			sb.append("MPSwitch\t[")
			  .append(sdf.format(now))
			  .append("]")
			  .append("\t")
			  .append(kind)
			  .append("\t")
			  .append(marketPlace.getGroup().getCompositeCourseID())
			  .append("$")
			  .append(marketPlace.getGroup().getRelativePath())
			  .append("\t")
			  .append(firstUser.getJmbag())
			  .append("\t")
			  .append(firstGroup.getRelativePath())
			  .append("\t")
			  .append(firstGroup.getName())
			  .append("\t")
			  .append(secondUser.getJmbag())
			  .append("\t")
			  .append(secondGroup.getRelativePath())
			  .append("\t")
			  .append(secondGroup.getName())
			  .append("\n")
			  ;
			try { w.write(sb.toString()); } catch(Exception ignorable) {}
			dirty = true;
		}
	}

	public void mpLogMove(MarketPlace marketPlace, User user, Group fromGroup, Group toGroup, User movedByUser) {
		if(w==null) return;
		Date now = new Date();
		synchronized(w) {
			StringBuilder sb = new StringBuilder(256);
			sb.append("MPMove\t[")
			  .append(sdf.format(now))
			  .append("]")
			  .append("\t")
			  .append(marketPlace.getGroup().getCompositeCourseID())
			  .append("$")
			  .append(marketPlace.getGroup().getRelativePath())
			  .append("\t")
			  .append(user.getJmbag())
			  .append("\t")
			  .append(fromGroup.getRelativePath())
			  .append("\t")
			  .append(fromGroup.getName())
			  .append("\t")
			  .append(toGroup.getRelativePath())
			  .append("\t")
			  .append(toGroup.getName())
			  .append("\t")
			  .append(movedByUser==null ? "-" : movedByUser.getJmbag())
			  .append("\n")
			  ;
			try { w.write(sb.toString()); } catch(Exception ignorable) {}
			dirty = true;
		}
	}
}
