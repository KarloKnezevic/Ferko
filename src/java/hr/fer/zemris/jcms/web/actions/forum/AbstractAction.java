package hr.fer.zemris.jcms.web.actions.forum;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.exceptions.NotLoggedInException;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subscription;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserAware;

import javax.persistence.EntityManager;

import com.opensymphony.xwork2.Preparable;

/**
 * Osnovni razred za sve akcije foruma.
 * 
 * @author Hrvoje Ban
 */
@SuppressWarnings("serial")
public class AbstractAction extends ExtendedActionSupport implements Preparable, CurrentUserAware {
	
	/**
	 * Ako su podaci uspješno spremljeni u bazu padataka i treba napraviti
	 * redirect na stranicu sa prikazom.
	 */
	public static final String UPDATE = "update";
	
	/**
	 * Ovaj razred je potreban radi iscrtavanja navigacije kolegija i
	 * prikaza stranice s greškama.
	 * 
	 * @author Hrvoje Ban
	 */
	public static class Data {
		
		private CourseInstance courseInstance;
		private IMessageLogger messageLogger;
		
		/**
		 * @return Instanca kolegija kojemu pripada trenutna kategorija.
		 * Može biti null;
		 */
		public CourseInstance getCourseInstance() {
			return courseInstance;
		}
		
		public void setCourseInstance(CourseInstance courseInstance) {
			this.courseInstance = courseInstance;
		}
		
		/**
		 * @return MessageLogger za dodavanje poruka o greškama.
		 */
		public IMessageLogger getMessageLogger() {
			return messageLogger;
		}
		
		public void setMessageLogger(IMessageLogger messageLogger) {
			this.messageLogger = messageLogger;
		}
	}
	
	private User loggedUser;
	private IJCMSSecurityManager securityManager;
	private Subscription subscription;
	private String courseInstanceID;
	private Data data;
	
	public AbstractAction() {
		data = new Data();
		data.setMessageLogger(MessageLoggerFactory.createMessageLogger(this));
	}
	
	/**
	 * Otvara transakciju i dohvaća podatke o trenutno logiranom korisniku.
	 * Razredi koji nasljeđuju ovaj razred će najčešće pregaziti ovu metodu.
	 */
	@Override
	public void prepare() throws Exception {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				prepare(em, null);
				return null;
			}
		});
	}

	/**
	 * Dohvaća podatke o trenutno logiranom korisniku unutar već otvorene
	 * transakcije. Ovu metodu pozivaju razredi koji nasljeđuju ovaj razred.
	 * @param em EntityManager već otvorena transakcije.
	 * @param category Kategorija nad kojoj se obavlja priprema. Može biti null.
	 */
	protected void prepare(EntityManager em, Category category) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if (getCurrentUser() == null)
			throw new NotLoggedInException();
			
		loggedUser = dh.getUserDAO().getUserById(em, getCurrentUser().getUserID());
		securityManager = JCMSSecurityManagerFactory.getManager();
		securityManager.init(loggedUser, em);
		
		if (category != null) {
			CourseInstance ci = null;
			subscription = dh.getForumDAO().findUserSubscription(em, loggedUser, category);
		
			if (category.getCourse() != null) {		
				if (subscription != null) {
					ci = subscription.getCourseInstance();
					courseInstanceID = null; // Ignoriramo paramater ako imamo pretplatu.
				} else if (courseInstanceID != null) {
					ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
					if (ci == null || !category.getCourse().equals(ci.getCourse()))
						throw new IllegalParameterException();
				} else
					ci = dh.getCourseInstanceDAO().findLastForCourse(em, category.getCourse());
				
				data.setCourseInstance(ci);
			}
		}
		
	}
	
	/**
	 * @return Korisnik koji pristupa ovaj akciji.
	 */
	public User getLoggedUser() {
		return loggedUser;
	}
	
	/**
	 * @return SecurityManager za logiranog korisnika.
	 */
	public IJCMSSecurityManager getSecurityManager() {
		return securityManager;
	}
	
	/**
	 * @return Pretplata logiranog korisnika na trenutnu kategoriju. null ako
	 * nema pretplate.
	 */
	public Subscription getSubscription() {
		return subscription;
	}
	
    /**
     * @return ID insance kolegija kojemu pripada trenutna kategorija.
     */
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    /**
     * @return URL parametar za courseInstanceID. Prazan niz ako trenutna
     * kategorija ne pripada ni jednom kolegiju. Koristi se za generiranje
     * URL za redirect.
     */
	public String getCourseInstanceIDParam() {
    	return (courseInstanceID != null) ? ("&courseInstanceID=" + courseInstanceID) : "";
    }
    
    public void setCourseInstanceID(String courseInstanceID) {
    	if (!"".equals(courseInstanceID))
    		this.courseInstanceID = courseInstanceID;
	}
	
	public Data getData() {
		return data;
	}

}
