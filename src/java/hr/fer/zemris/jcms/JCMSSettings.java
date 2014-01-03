package hr.fer.zemris.jcms;

import hr.fer.zemris.jcms.activities.IActivityReporter;
import hr.fer.zemris.jcms.activities.IActivityServiceProvider;
import hr.fer.zemris.jcms.activities.impl.ActivityServiceProviderImpl;
import hr.fer.zemris.jcms.locking.ILockManager;
import hr.fer.zemris.jcms.locking.impl.InProcessLockManager;
import hr.fer.zemris.jcms.periodicals.IPeriodicalService;
import hr.fer.zemris.jcms.periodicals.PeriodicalServiceDescriptor;
import hr.fer.zemris.jcms.web.actions.Struts2TextProvider;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Razred koji čuva se konfiguracijske detalje JCMS-a, tipa vršni direktorij
 * za pohranu datoteka i sl.
 * 
 * @author marcupic
 *
 */
public class JCMSSettings {
	
	private static final Logger logger = Logger.getLogger(JCMSSettings.class.getCanonicalName());

	private static JCMSSettings settings = null;
	private File filesRootDir;
	private File rootDir;
	private File repositoriesRootDir;
	private File repositoriesImageCacheRootDir;
	private File compRootDir;
	private File systemLogsDir;
	private File mpLogsDir;
	private boolean debugMode;
	private File assessmentStatsRootDir;
	PeriodicalServiceDescriptor[] periodicalServices;
	private ILockManager lockManager;
	private IActivityReporter activityReporter;
	private boolean smtpServerEnabled;
	private String noreplyEMail;
	private String smtpServer;
	
	private Map<String,Object> objects = new HashMap<String, Object>(); 

	public JCMSSettings() {
		lockManager = new InProcessLockManager();
		Properties prop = new Properties();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("jcms.properties");
		if(is!=null) {
			logger.info("jcms.properties found.");
			try {
				prop.load(is);
			} catch(Exception ex) {
				logger.error(ex);
			}
		} else {
			logger.info("jcms.properties not found.");
		}
		if(is!=null) try { is.close(); } catch(Exception ignorable) {}
		readProps(prop);
		prop = new Properties();
		is = this.getClass().getClassLoader().getResourceAsStream("jcms-periodicals.properties");
		if(is!=null) {
			logger.info("jcms-periodicals.properties found.");
			try {
				periodicalsLoad(is);
			} catch(Exception ex) {
				logger.error(ex);
			}
		} else {
			logger.info("jcms-periodicals.properties not found.");
		}
		if(is!=null) try { is.close(); } catch(Exception ignorable) {}
		IActivityServiceProvider iasp = new ActivityServiceProviderImpl();
		this.getObjects().put("activityWorker", iasp.getActivityWorker());
		this.activityReporter = iasp.getActivityReporter();
		this.getObjects().put("activityReporter", activityReporter);
	}

	/**
	 * Vraća logger koji nije vezan niti uz jednu akciju, i koji se smije koristiti
	 * samo za potrebe prevođenja izraza na različite jezike. Svaki poziv ove metode stvara
	 * novi logger (iz dokumentacije struts2 nije jasno inače je li njihov objekt koji i mi
	 * ovdje ovdje koristimo thread-safe).
	 * 
	 * @return logger za internacionalizaciju
	 */
	public IMessageLogger getI18nLogger() {
		return new Struts2TextProvider();
	}
	
	public boolean isSmtpServerEnabled() {
		return smtpServerEnabled;
	}
	public String getSmtpServer() {
		return smtpServer;
	}
	
	/**
	 * Dohvat objekta koji se koristi za objavu događaja.
	 * 
	 * @return objekt za objavu događaja
	 */
	public IActivityReporter getActivityReporter() {
		return activityReporter;
	}
	
	private void periodicalsLoad(InputStream is) throws IOException {
		List<PeriodicalServiceDescriptor> list = new ArrayList<PeriodicalServiceDescriptor>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.isEmpty()) continue;
			if(line.charAt(0)=='#') continue;
			String[] elems = StringUtil.split(line, '\t');
			if(elems.length<2) {
				logger.error("[jcms-periodicals.properties] Skipping line due to misformat: "+line);
				continue;
			}
			long[] callPoints = new long[elems.length-1];
			try {
				for(int i = 0; i < callPoints.length; i++) {
					callPoints[i] = parseCallPoint(elems[i+1]);
				}
			} catch(Exception ex) {
				logger.error("[jcms-periodicals.properties] Invalid code point for service "+elems[0]+". Service skipped.");
				continue;
			}
			try {
				IPeriodicalService service = (IPeriodicalService)this.getClass().getClassLoader().loadClass(elems[0]).newInstance();
				list.add(new PeriodicalServiceDescriptor(elems[0],service,callPoints));
			} catch(Throwable ex) {
				logger.error("[jcms-periodicals.properties] Error instantiating service "+elems[0]+".", ex);
				continue;
			}
		}
		periodicalServices = new PeriodicalServiceDescriptor[list.size()];
		list.toArray(periodicalServices);
	}

	/**
	 * Vraća broj milisekundi od ponoći do zadanog vremena u formatu HH:mm.
	 * 
	 * @param when vrijeme
	 * @return broj milisekundi od ponoći
	 * @throws Exception u slučaju greške kod parsiranja
	 */
	private long parseCallPoint(String when) throws Exception {
		if(when.length()!=5 || when.charAt(2)!=':') {
			throw new ParseException("Neispravan format vremena "+when+". Očekivao sam HH:mm.", 0);
		}
		return (Long.parseLong(when.substring(0,2))*3600+Long.parseLong(when.substring(3,5))*60)*1000L;
	}

	private void readProps(Properties prop) {
		smtpServerEnabled = false;
		if(prop.getProperty("jcms.smtp.enabled", "false").equals("true")) {
			String s = prop.getProperty("jcms.smtp.server", null);
			if(!StringUtil.isStringBlank(s)) {
				String nrm = prop.getProperty("jcms.smtp.ferko-email.nr", null);
				if(!StringUtil.isStringBlank(nrm)) {
					noreplyEMail = nrm;
					smtpServerEnabled = true;
					smtpServer = s;
				}
			}
		}
		if(!smtpServerEnabled) {
			smtpServer = null;
			noreplyEMail = null;
		}
		getObjects().put("jcms.questionBrowser.enabled", prop.getProperty("jcms.questionBrowser.enabled", null));
		getObjects().put("jcms.external.labosi.enabled", prop.getProperty("jcms.external.labosi.enabled", null));
		getObjects().put("jcms.external.labosi.url", prop.getProperty("jcms.external.labosi.url", null));
		String studtest2DataURLService = prop.getProperty("studtest2.dataURLService");
		if(studtest2DataURLService!=null && studtest2DataURLService.length()>0) {
			getObjects().put("studtest2.dataURLService", studtest2DataURLService);
		} else {
			logger.warn("[JCMS] studtest2.dataURLService is not set, so testing will be disabled.");
		}
		debugMode = "true".equals(prop.getProperty("debugMode", "true"));
		logger.info("[JCMS] debug mode = "+debugMode);
		String rootDirName = prop.getProperty("rootDir");
		File rootDir;
		if(StringUtil.isStringBlank(rootDirName)) {
			rootDirName = System.getProperty("java.io.tmpdir");
			File tmpDir = new File(rootDirName);
			rootDir = new File(tmpDir,"jcms");
		} else {
			rootDir = new File(rootDirName);
		}
		rootDir.mkdir();
		File filesRootDir = new File(rootDir,"jcms-asfiles");
		filesRootDir.mkdir();
		File repositoriesRootDir = new File(rootDir,"jcms-repos");
		repositoriesRootDir.mkdir();
		File repositoriesImageCacheRootDir = new File (rootDir, "jcms-image-cache");
		repositoriesImageCacheRootDir.mkdir();
		File systemLogsDir = new File(rootDir, "system-logs");
		systemLogsDir.mkdir();
		File mpLogsDir = new File(systemLogsDir, "mps");
		mpLogsDir.mkdir();
		File compRootDir = new File(rootDir,"jcms-components");
		compRootDir.mkdir();
		File assessmentStatsRootDir = new File(rootDir,"as-stats");
		assessmentStatsRootDir.mkdir();

		this.filesRootDir = filesRootDir;
		this.rootDir = rootDir;
		this.repositoriesRootDir = repositoriesRootDir;
		this.repositoriesImageCacheRootDir = repositoriesImageCacheRootDir;
		this.compRootDir = compRootDir;
		this.systemLogsDir = systemLogsDir;
		this.mpLogsDir = mpLogsDir;
		this.assessmentStatsRootDir = assessmentStatsRootDir;
		
		logger.info("[JCMS] root dir = "+rootDir);
		logger.info("[JCMS] files root dir = "+filesRootDir);
		logger.info("[JCMS] repositories root dir = "+repositoriesRootDir);
		logger.info("[JCMS] repositories image cache root dir = "+repositoriesImageCacheRootDir);
		logger.info("[JCMS] task files root dir = "+compRootDir);
		logger.info("[JCMS] system logs dir = "+systemLogsDir);
		logger.info("[JCMS] mp logs dir = "+mpLogsDir);
		logger.info("[JCMS] assessment stats dir = "+assessmentStatsRootDir);
		logger.info("[JCMS] smtp server enabled = "+isSmtpServerEnabled());
		logger.info("[JCMS] smtp server = "+getSmtpServer());
		logger.info("[JCMS] no-reply email = "+getNoreplyEMail());
	}

	public String getNoreplyEMail() {
		return noreplyEMail;
	}
	
	/**
	 * Vraća {@link ILockManager} objekt koji se može koristiti
	 * za zaključavanje.
	 * 
	 * @return lockManager
	 */
	public ILockManager getLockManager() {
		return lockManager;
	}
	
	public Map<String, Object> getObjects() {
		return objects;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}
	
	public File getMpLogsDir() {
		return mpLogsDir;
	}
	
	public File getSystemLogsDir() {
		return systemLogsDir;
	}
	
	public static JCMSSettings getSettings() {
		return settings;
	}

	public String getApplRealPath() {
		return (String)getObjects().get("applRealPath");
	}
	
	public File getFilesRootDir() {
		return filesRootDir;
	}

	public File getCompRootDir() {
		return compRootDir;
	}

	public File getRootDir() {
		return rootDir;
	}

	public File getAssessmentStatsRootDir() {
		return assessmentStatsRootDir;
	}
	
	public File getRepositoriesRootDir(){
		return repositoriesRootDir;
	}
	
	public File getRepositoriesImageCacheRootDir() {
		return repositoriesImageCacheRootDir;
	}
	
	public static void init() {
		settings = new JCMSSettings();
	}

	
}
