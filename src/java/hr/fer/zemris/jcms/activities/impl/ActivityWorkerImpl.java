package hr.fer.zemris.jcms.activities.impl;

import java.io.BufferedInputStream;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.AbstractActivity;
import hr.fer.zemris.jcms.activities.AbstractCourseActivity;
import hr.fer.zemris.jcms.activities.IActivityWorker;
import hr.fer.zemris.jcms.activities.types.ApplicationActivity;
import hr.fer.zemris.jcms.activities.types.ComponentGroupActivity;
import hr.fer.zemris.jcms.activities.types.GradeActivity;
import hr.fer.zemris.jcms.activities.types.GroupActivity;
import hr.fer.zemris.jcms.activities.types.IssueTrackingActivity;
import hr.fer.zemris.jcms.activities.types.MarketActivity;
import hr.fer.zemris.jcms.activities.types.ScoreActivity;
import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Activity;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserActivityPrefs;
import hr.fer.zemris.jcms.service2.ActivityService;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

/**
 * Implementacija koja aktivnosti za objavu drži u zasebnom direktoriju
 * u serijaliziranom obliku. Svaki posao (niz aktivnosti koje treba objaviti)
 * zapisan je u jednoj datoteci. Vrsta posla se pri tome prepoznaje po
 * ekstenzijama, kako slijedi.
 * 
 * <ul>
 * <li><b><code>.acn</code></b> - datoteka započete sjednice; ako se ovo nađe prilikom bootanja,
 *     znači da netko nije prije završio posao, a to je loše!</li>
 * <li><b><code>.acc</code></b> - datoteka commitane sjednice; ako se ovo nađe prilikom bootanja,
 *     treba dodati u red poslova koje treba napraviti.</li>
 * <li><b><code>.acs</code></b> - datoteka započete obrade; ako se ovo nađe prilikom bootanja,
 *     znači da je posao objave vjerojatno pukao, jer je to nakon objave trebalo biti obrisano.
 *     Kako god, treba obrisati.</li>
 * </ul>
 * 
 * @author marcupic
 *
 */
public class ActivityWorkerImpl implements IActivityWorker, Runnable {

	private boolean stopRequested = false;
	private Thread thread;
	private LinkedList<JobRequest> jobs = new LinkedList<JobRequest>();
	private File baseDir;
	private Map<String,IActivityPublishProcessor> processors;

	// Ovdje treba registrirati sve procesore za objavu razlicitih vrsta aktivnosti
	public ActivityWorkerImpl() {
		processors = new HashMap<String, IActivityPublishProcessor>();
		registerProcessor(ApplicationActivity.class, new ApplicationActivityPublishProcessor());
		registerProcessor(GradeActivity.class, new GradeActivityPublishProcessor());
		registerProcessor(ScoreActivity.class, new ScoreActivityPublishProcessor());
		registerProcessor(MarketActivity.class, new MarketActivityPublishProcessor());
		registerProcessor(GroupActivity.class, new GroupActivityPublishProcessor());
		registerProcessor(ComponentGroupActivity.class, new GroupActivityPublishProcessor());
		registerProcessor(IssueTrackingActivity.class, new IssueTrackingActivityPublishProcessor());
	}

	private void registerProcessor(Class<?> activityClass, IActivityPublishProcessor processor) {
		String name = activityClass.getName();
		processors.put(name, processor);
	}
	
	@Override
	public void start() {
		baseDir = new File(JCMSSettings.getSettings().getRootDir(), "activities");
		if(!baseDir.exists()) {
			baseDir.mkdir();
		}
		synchronized (this) {
			stopRequested = false;
			if(thread==null) {
				prepareDirectory();
				thread = new Thread(this, "ActivityWorker");
				thread.start();
			}
		}
	}

	private void prepareDirectory() {
		for(File f : baseDir.listFiles()) {
			if(!f.isFile()) continue;
			String name = f.getName();
			if(name.endsWith(".acn")) {
				f.delete();
				continue;
			}
			if(name.endsWith(".acs")) {
				f.delete();
				continue;
			}
			if(name.endsWith(".acc")) {
				JobRequest jr = new JobRequest(f);
				jobs.add(jr);
				System.out.println("Dodajem zaostali posao objave aktivnosti: "+name);
				continue;
			}
		}
	}

	@Override
	public void stop() {
		Thread waitThread = null;
		synchronized (this) {
			stopRequested = true;
			notifyAll();
			waitThread = thread;
		}
		if(waitThread!=null) {
			try {
				waitThread.join();
			} catch (InterruptedException ignorable) {
			}
		}
		
	}

	@Override
	public void run() {
		while(true) {
			JobRequest req = getJobRequest();
			if(req==null) break;
			try {
				processJob(req);
			} catch(Exception ex) {
				System.out.println("Could not finish broadcasting of activities: "+req);
			} finally {
				req.destroyRequest();
			}
		}
		synchronized (this) {
			thread = null;
		}
	}

	private void processJob(JobRequest req) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(req.file)));
		} catch(Exception ex) {
			ex.printStackTrace();
			return;
		}
		Map<String,Object> cache = new HashMap<String, Object>(128);
		IMessageLogger logger = JCMSSettings.getSettings().getI18nLogger();
		while(true) {
			AbstractActivity activity = null;
			try {
				activity = (AbstractActivity)ois.readObject();
			} catch(EOFException ex) {
				// Gotovi smo
				break;
			} catch(Exception ex) {
				// A ovo je neka gadna greška; ništa, opet smo gotovi
				ex.printStackTrace();
				break;
			}
			try {
				IActivityPublishProcessor processor = processors.get(activity.getClass().getName());
				processor.publish(activity, cache, logger);
			} catch(Exception ex) {
				System.out.println("Could not publish activity: "+activity+"; reason: "+ex.getMessage());
			}
		}
		try { ois.close(); } catch(Exception ignorable) {}
		req.destroyRequest();
	}

	synchronized JobRequest createJobRequest() {
		File f = null;
		try {
			f = File.createTempFile("act", ".acn", baseDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JobRequest(f);
	}
	
	private synchronized JobRequest getJobRequest() {
		if(stopRequested) return null;
		while(jobs.isEmpty()) {
			try {
				wait(20000);
			} catch (InterruptedException ignorable) {
			}
			if(stopRequested) return null;
		}
		return jobs.removeFirst();
	}

	synchronized void queueJobRequest(JobRequest request) {
		jobs.addLast(request);
		notifyAll();
	}

	static class JobRequest {
		File file;
		
		public JobRequest(File file) {
			this.file = file;
		}
		
		public void destroyRequest() {
			if(file!=null) {
				file.delete();
				file = null;
			}
		}
		
		/**
		 * Mijenja ime datoteci. Ako ne uspije, briše je i vraća <code>false</code>; inaće vraća <code>true</code>.
		 * 
		 * @param ext nova ekstenzija (s točkom na početku)
		 * @return <code>true</code> za uspjeh, <code>false</code> inače
		 */
		public boolean setExtension(String ext) {
			String name = file.getName();
			int pos = name.lastIndexOf('.');
			if(pos!=-1) {
				name = name.substring(0, pos);
			}
			name = name + ext;
			File newFile = new File(file.getParentFile(), name);
			if(!file.renameTo(newFile)) {
				file.delete();
				return false;
			}
			file = newFile;
			return true;
		}
	}
	
	/**
	 * Sučelje koje moraju implementirati svi objekti koji znaju objavljivati aktivnosti.
	 * 
	 * @author marcupic
	 */
	private static interface IActivityPublishProcessor {
		/**
		 * Ovo je temeljna metoda koja služi objavi svih aktivnosti.
		 * 
		 * @param activity aktivnost
		 * @param cache priručni cache objekata
		 */
		void publish(AbstractActivity activity, Map<String, Object> cache, IMessageLogger logger);
	}
	
	private static class Pointer<T> {
		private T value;
		
		public T getValue() {
			return value;
		}
		public void setValue(T value) {
			this.value = value;
		}
	}
	
	/**
	 * Apstraktna implementacija sučelja za objavu aktivnosti. Služi kao temelj za izvođenje
	 * svih ostalih aktivnosti.
	 * 
	 * @author marcupic
	 */
	private static abstract class AbstractActivityPublishProcessor implements IActivityPublishProcessor {
		public void publish(final AbstractActivity activity, final Map<String, Object> cache, final IMessageLogger logger) {
			PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
				@Override
				public Void executeOperation(EntityManager em) {
					User user = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, activity.getUserID());
					Pointer<Activity> ptr = new Pointer<Activity>();
					publishActivity(em, activity, user, cache, ptr);
					if(ptr.getValue()!=null) {
						sendMail(activity, ptr.getValue(), user, cache, logger);
					}
					return null;
				}
			});
		}
		
		/**
		 * Ovu metodu trebaju implementirati svi razredi koji temeljem ovog razreda objavljuju aktivnosti.
		 * 
		 * @param em entity manager
		 * @param activity aktivnost
		 * @param user korisnik
		 * @param cache priručni cache objekata
		 * @param ptr pokazivač na objekt modela koji će se spremiti u bazu
		 */
		protected abstract void publishActivity(EntityManager em, AbstractActivity activity, User user, Map<String, Object> cache, Pointer<Activity> ptr);
		
		private void sendMail(AbstractActivity activity, Activity modelActivity, User user, Map<String, Object> cache, IMessageLogger logger) {
			if(!JCMSSettings.getSettings().isSmtpServerEnabled()) return;
			try {
				UserActivityPrefs uap = user.getUserDescriptor().getUserActivityPrefs();
				if(uap!=null) {
					Properties p = StringUtil.getPropertiesFromString(uap.getProperties());
					if("true".equals(p.getProperty("mail", "0"))) {
						if(checkEMailAddress(user.getUserDescriptor().getEmail())) {
							String[] data = getEMailData(activity, modelActivity, cache, logger);
							boolean preskoci = false;
							if(data==null || data.length<2) preskoci = true;
							if(!preskoci) {
								for(String s : data) {
									if(StringUtil.isStringBlank(s)) {
										preskoci = true;
										break;
									}
								}
							}
							if(preskoci) return;
							String subject = data[0];
							String[] messages = Arrays.copyOfRange(data, 1, data.length);
							Properties mailProp = new Properties();
							mailProp.put("mail.smtp.host", JCMSSettings.getSettings().getSmtpServer());
							Session session = Session.getInstance(mailProp, null);
							Message msg = new MimeMessage(session);
							InternetAddress from = new InternetAddress(JCMSSettings.getSettings().getNoreplyEMail());
							msg.setFrom(from);
							msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getUserDescriptor().getEmail()));
							msg.setSubject("[Ferko] "+subject);
							msg.setContent(messages[0], "text/plain; charset=utf-8");
							Transport.send(msg);
						}
					}
				}
			} catch(Exception ex) {
				System.out.println("[MAIL-ERROR]: Could not send activity notification to user (id="+user.getId()+", mail="+user.getUserDescriptor().getEmail()+"). Reason: "+ex.getMessage());
			}
		}

		/**
		 * Metoda koja mora vratiti naslov poruke te tijelo poruke koju treba
		 * poslati. Očekuje se da će polje koje se vraća imati dva ili tri
		 * elementa. Prvi element se uvijek tumači kao naslov poruke. Ako polje
		 * ima dva elementa, pretpostavlja se da je drugi element text/plain
		 * format poruke. Ako ima tri elementa, drugi se tumači kao text/plain,
		 * a treći kao text/html format tijela poruke koju treba poslati. Ako se
		 * vrati <code>null</code>, ili polje od jednog ili više elemenata u
		 * kojem je neki element <code>null</code> ili prazan, mail se neće
		 * poslati. Primjer:<br>
		 * <br>
		 * <code>return new String[] {"Stigle ocjene!", "Dobili ste ocjenu 5.", "Dobili ste ocjenu 5. Možete je pogledati u <a href='https://ferko.fer.hr/ferko'>Ferku</a>."};</code>
		 * 
		 * @param activity
		 *            aktivnost
		 * @param modelActivity
		 *            objekt modela koji predstavlja trenutnu aktivnost
		 * @param cache
		 *            priručni cache objekata
		 * @param logger
		 *            objekt za internacionalizaciju
		 * @return polje s podatcima o e-mailu
		 */
		protected abstract String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger);
		
		private boolean checkEMailAddress(String email) {
			if(StringUtil.isStringBlank(email)) return false;
			int pos = email.indexOf('@');
			if(pos==-1 || pos==0 || pos==email.length()-1) return false;
			return true;
		}
	}
	
	private static abstract class CourseActivityPublishProcessor extends AbstractActivityPublishProcessor {
		protected void publishActivity(EntityManager em, AbstractActivity activity, User user, Map<String, Object> cache, Pointer<Activity> ptr) {
			AbstractCourseActivity acActivity = (AbstractCourseActivity)activity;
			String cid = "cid."+acActivity.getCourseInstanceID();
			CourseInstance ci = (CourseInstance)cache.get(cid);
			if(ci==null) {
				ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, acActivity.getCourseInstanceID());
				cache.put(cid, ci);
			}
			if(ci==null) {
				System.out.println("Unable to publish activity. CourseInstance id="+acActivity.getCourseInstanceID()+" not found.");
				return;
			}
			publishCourseActivity(em, acActivity, user, ci, cache, ptr);
		}
		protected abstract void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr);
	}
	
	private static class ApplicationActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			ApplicationActivity gActivity = (ApplicationActivity)activity;
			Activity a = new Activity();
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("A");
			a.setUser(user);
			StudentApplication stapl = DAOHelperFactory.getDAOHelper().getApplicationDAO().get(em, gActivity.getStudentApplicationId());
			if(stapl==null) {
				throw new RuntimeException("Prijava studenta je nedostupna.");
			}
			a.setData(gActivity.getStudentApplicationId()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName()+"\t"+stapl.getApplicationDefinition().getId()+"\t"+stapl.getApplicationDefinition().getName());
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);

			String[] elems = StringUtil.split(modelActivity.getData(), '\t');
			
			String subject = logger.getText("activity.msg.application.t", new String[] {elems[4]});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
	
	private static class GradeActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			GradeActivity gActivity = (GradeActivity)activity;
			Activity a = new Activity();
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("G");
			a.setUser(user);
			a.setData(gActivity.getGrade()+"\t"+gActivity.getRang()+"\t"+gActivity.getKind().name()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName());
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			GradeActivity gActivity = (GradeActivity)activity;
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
			CourseInstance ci = (CourseInstance)cache.get("cid."+gActivity.getCourseInstanceID());
			String subject = logger.getText("activity.msg.grade.t", new String[] {ci.getCourse().getName()});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
	
	private static class IssueTrackingActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			IssueTrackingActivity iActivity = (IssueTrackingActivity)activity;
			Activity a = new Activity();
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("I");
			a.setUser(user);
			//Tko je poslao - Što je poslao - ID pitanja - Ciljani korisnik
			a.setData(iActivity.getUsername()+"\t"+iActivity.getKind()+"\t"+iActivity.getIssueID()+"\t"+iActivity.getUserID()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName());
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			IssueTrackingActivity iActivity = (IssueTrackingActivity)activity;
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
			CourseInstance ci = (CourseInstance)cache.get("cid."+iActivity.getCourseInstanceID());
			String subject = logger.getText("activity.msg.its."+iActivity.getKind(), new String[] {iActivity.getUsername(), ci.getCourse().getName(), iActivity.getIssueID().toString()});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
	
	private static class MarketActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			MarketActivity gActivity = (MarketActivity)activity;
			Activity a = new Activity();
			String gid = "gid."+gActivity.getParentGroupID();
			Group g = (Group)cache.get(gid);
			if(g==null) {
				g = DAOHelperFactory.getDAOHelper().getGroupDAO().get(em, gActivity.getParentGroupID());
				if(g==null) {
					System.out.println("Unable to publish activity. Group id="+gActivity.getParentGroupID()+" not found.");
					return;
				}
				cache.put(gid, g);
			}
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("M");
			a.setUser(user);
			a.setData(gActivity.getKind()+"\t"+gActivity.getParentGroupID()+"\t"+gActivity.getUsername()+"\t"+gActivity.getGroupName()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName()+"\t"+g.getName());
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			MarketActivity gActivity = (MarketActivity)activity;
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
			CourseInstance ci = (CourseInstance)cache.get("cid."+gActivity.getCourseInstanceID());
			String subject = logger.getText("activity.msg.market.t", new String[] {ci.getCourse().getName()});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
	
	private static class GroupActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			GroupActivity gActivity = (GroupActivity)activity;
			Activity a = new Activity();
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("R");
			a.setUser(user);
			// Ovo je prezan appendix; ako se ne override-a, student se preusmjeruje na stranicu kolegija i generira se defaultna poruka.
			String appendix = "\t_";
			if(activity instanceof ComponentGroupActivity) {
				ComponentGroupActivity c = (ComponentGroupActivity)gActivity;
				appendix = "\tC\t"+c.getGroupRoot()+"\t"+c.getItemPosition();
			}
			a.setData(gActivity.getKind()+"\t"+gActivity.getParentGroupName()+"\t"+gActivity.getGroupName()+"\t"+courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName()+appendix);
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			GroupActivity gActivity = (GroupActivity)activity;
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
			CourseInstance ci = (CourseInstance)cache.get("cid."+gActivity.getCourseInstanceID());
			String subject = logger.getText("activity.msg.group.t", new String[] {ci.getCourse().getName()});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
	
	private static class ScoreActivityPublishProcessor extends CourseActivityPublishProcessor {
		protected void publishCourseActivity(EntityManager em, AbstractCourseActivity activity, User user, CourseInstance courseInstance, Map<String, Object> cache, Pointer<Activity> ptr) {
			ScoreActivity sActivity = (ScoreActivity)activity;
			Activity a = new Activity();
			a.setContext("cid="+courseInstance.getId());
			a.setDate(activity.getDate());
			a.setKind("S");
			a.setUser(user);
			List<String> list = sActivity.getComponents();
			StringBuilder sb = new StringBuilder(courseInstance.getCourse().getIsvuCode()+"\t"+courseInstance.getCourse().getName());
			for(String comp : list) {
				sb.append('\t');
				// Ako mi više ne stane u bazu, dodaj znak pokrate i prekini...
				if(sb.length()+comp.length()>1024) {
					sb.append(".");
					break;
				} else {
					sb.append(comp);
				}
			}
			a.setData(sb.toString());
			DAOHelperFactory.getDAOHelper().getActivityDAO().save(em, a);
			ptr.setValue(a);
		}

		@Override
		protected String[] getEMailData(AbstractActivity activity, Activity modelActivity, Map<String, Object> cache, IMessageLogger logger) {
			ScoreActivity sActivity = (ScoreActivity)activity;
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.dateTimeFormat);
			CourseInstance ci = (CourseInstance)cache.get("cid."+sActivity.getCourseInstanceID());
			String subject = logger.getText("activity.msg.score.t", new String[] {ci.getCourse().getName()});
			ActivityBean aBean = ActivityService.generateActivityBean(modelActivity, logger);
			String msg = logger.getText("date")+": "+sdf.format(aBean.getDate())+"\r\n\r\n"+aBean.getMessage()+"\r\n\r\n"+logger.getText("sig.1");
			return new String[] {subject, msg};
		}
	}
}
