package hr.fer.zemris.jcms.web.listeners;

import hr.fer.zemris.jcms.JCMSLogger;
import hr.fer.zemris.jcms.JCMSServices;
import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentStatisticsService;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesStore;
import hr.fer.zemris.jcms.wiki.WikiParserFactory;
import hr.fer.zemris.studtest2.web.cp.StudTest2CPInitializer;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ApplicationListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		DelayedMessagesStore messageStore = (DelayedMessagesStore)event.getServletContext().getAttribute("jcms.messageStore");
		if(messageStore!=null) {
			event.getServletContext().removeAttribute("jcms.messageStore");
		}
		JCMSServices.stop();
		AssessmentStatisticsService.destroy();
		StudTest2CPInitializer.shutdownConnectionPool(event.getServletContext(), "studtest2-cpool");
		System.out.println("[ApplicationListener] Destroying entity manager factory...");
		EntityManagerFactory emf = (EntityManagerFactory)event.getServletContext().getAttribute("jcmsdb.entityManagerFactory");
		if(emf!=null) {
			event.getServletContext().removeAttribute("jcmsdb.entityManagerFactory");
			emf.close();
			PersistenceUtil.clearSingleTon();
			System.out.println("[ApplicationListener] Destroying entity manager factory finished.");
		} else {
			System.out.println("[ApplicationListener] No entity manager factory found!");
		}
		JCMSLogger.stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		JCMSSettings.init();
		String applRealPath = event.getServletContext().getRealPath("/");
		JCMSSettings.getSettings().getObjects().put("applRealPath", applRealPath);
		JCMSLogger.getLogger();
		StudTest2CPInitializer.setupConnectionPool(event.getServletContext(), "studtest2-cpool", "/WEB-INF/studtest2-conn.properties");
		JCMSSettings.getSettings().getObjects().put("studtest2-cpool", event.getServletContext().getAttribute("studtest2-cpool"));
		JCMSSecurityManagerFactory.init();
		DelayedMessagesStore messageStore = new DelayedMessagesStore();
		event.getServletContext().setAttribute("jcms.messageStore", messageStore);
		WikiParserFactory.init();
		System.out.println("[ApplicationListener] Creating entity manager factory...");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jcmsdb");
		event.getServletContext().setAttribute("jcmsdb.entityManagerFactory", emf);
		PersistenceUtil.initSingleTon(emf);
		System.out.println("[ApplicationListener] Creating entity manager factory finished.");
		try {
			JCMSCacheFactory.init();
			JCMSServices.start();
			AssessmentStatisticsService.init();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
