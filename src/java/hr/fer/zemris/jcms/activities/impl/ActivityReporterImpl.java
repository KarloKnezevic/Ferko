package hr.fer.zemris.jcms.activities.impl;

import java.io.BufferedOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import hr.fer.zemris.jcms.activities.AbstractActivity;
import hr.fer.zemris.jcms.activities.IActivityReporter;
import hr.fer.zemris.jcms.activities.impl.ActivityWorkerImpl.JobRequest;

public class ActivityReporterImpl implements IActivityReporter {

	private ActivityWorkerImpl worker;
	private ThreadLocal<ThreadData> locals = new ThreadLocal<ThreadData>();
	
	public ActivityReporterImpl(ActivityWorkerImpl worker) {
		this.worker = worker;
	}
	
	@Override
	public void addActivity(AbstractActivity activity) {
		ThreadData td = checkOpen(true);
		if(td.oos!=null) {
			try {
				td.oos.writeObject(activity);
			} catch (IOException e) {
				// Ovu grešku namjerno zanemarujemo; ako ne možemo dodati activity, preživjet ćemo.
				e.printStackTrace();
			}
		}
	}

	private ThreadData checkOpen(boolean autoCreate) {
		ThreadData td = locals.get();
		if(td==null) {
			if(!autoCreate) return null;
			td = new ThreadData();
			td.jreq = worker.createJobRequest();
			if(td.jreq.file!=null) {
				try {
					td.oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(td.jreq.file)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			locals.set(td);
		}
		return td;
	}

	@Override
	public void commitAndCloseSession() {
		// Je li uopće išta otvoreno?
		ThreadData td = checkOpen(false);
		// Ako nije, gotovi smo...
		if(td==null) return;
		// Makni to iz konteksta...
		locals.remove();
		// Ako je i ako nemam otvoreni stream, opet zanemarujemo...
		if(td.oos==null) {
			td.jreq.file.delete();
			return;
		}
		// Inače zatvori datoteku, preimenuj i predaj dalje na obradu:
		boolean obrisi = false;
		try { td.oos.flush(); } catch(Exception ex) { obrisi = true; }
		try { td.oos.close(); } catch(Exception ex) { obrisi = true; }
		if(obrisi) {
			td.jreq.file.delete();
			return;
		}
		if(!td.jreq.setExtension(".acc")) {
			System.out.println("Nisam uspio prebaciti aktivnosti iz .acn u .acc.");
			return;
		}
		worker.queueJobRequest(td.jreq);
	}

	@Override
	public boolean isSessionOpen() {
		return checkOpen(false)!=null;
	}

	@Override
	public void openSession() {
		checkOpen(true);
	}

	@Override
	public void rollbackAndCloseSession() {
		// Je li uopće išta otvoreno?
		ThreadData td = checkOpen(false);
		// Ako nije, gotovi smo...
		if(td==null) return;
		// Makni to iz konteksta...
		locals.remove();
		// Ako je i ako nemam otvoreni stream, opet zanemarujemo...
		if(td.oos==null) {
			td.jreq.file.delete();
			return;
		}
		// Inače zatvori datoteku, preimenuj i predaj dalje na obradu:
		try { td.oos.flush(); } catch(Exception ex) {}
		try { td.oos.close(); } catch(Exception ex) {}
		td.jreq.file.delete();
	}

	static class ThreadData {
		ObjectOutputStream oos;
		JobRequest jreq;
	}
}
