package hr.fer.zemris.jcms.web.actions.forum;

import java.util.List;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserAware;

@SuppressWarnings("serial")
public class UpdateForum extends ExtendedActionSupport implements CurrentUserAware {
	
	CurrentUser currentUser;
	
	@Override
	public String execute() throws Exception {
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<String>() {
		@Override
		public String executeOperation(EntityManager em) {
			if (currentUser == null)
				return NOT_LOGGED_IN;
			
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			IJCMSSecurityManager securityManager = JCMSSecurityManagerFactory.getManager();
			User user = dh.getUserDAO().getUserById(em, currentUser.getUserID());
			securityManager.init(user, em);
			
			if (!securityManager.canPerformSystemAdministration())
				return NO_PERMISSION;
			
			List<Course> courses = dh.getCourseDAO().getAllWithoutCategory(em);
			for (Course course : courses)
				SynchronizerService.createDefaultCategory(course, em);
			
			IMessageLogger messageLogger = MessageLoggerFactory.createMessageLogger(UpdateForum.this);
			messageLogger.addInfoMessage(getText("Forum.updateSuccessful"));
			messageLogger.registerAsDelayed();
			return SUCCESS;
		}});
	}
	
	@Override
	public void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

}
