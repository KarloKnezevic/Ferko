package hr.fer.zemris.jcms.web.actions.issues;

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserAware;

@SuppressWarnings("serial")
public class IssuesInitialization extends ExtendedActionSupport implements CurrentUserAware {
	
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
				
			IMessageLogger messageLogger = MessageLoggerFactory.createMessageLogger(IssuesInitialization.this);
			try{
				String yearSemesterID = dh.getKeyValueDAO().get(em, "currentSemester").getValue();
				List<CourseInstance> courseInstances = dh.getCourseInstanceDAO().findForSemester(em, yearSemesterID);
				for(CourseInstance ci : courseInstances){
					Set<CourseComponent> courseComponents = ci.getCourseComponents();
					//Inicijalizacija tema za sve komponente
					for(CourseComponent cc : courseComponents){
						IssueTrackingService.updateMessageTopic(em, cc.getDescriptor().getName(), cc.getDescriptor().getName(), cc, "CC");
						//Inicijalizacija za sve iteme komponente
						Set<CourseComponentItem> courseComponentItems = cc.getItems();
						for(CourseComponentItem cci : courseComponentItems){
							IssueTrackingService.updateMessageTopic(em, cci.getName(), cci.getName(), cc, "CCI");
						}
					}
				}
				messageLogger.addInfoMessage(getText("ITS.initSuccessful"));
				messageLogger.registerAsDelayed();
			}catch(Exception e){
				messageLogger.addInfoMessage(getText("ITS.initUnsuccessful"));
				messageLogger.registerAsDelayed();
			}
			return SUCCESS;
		}
		
		});
	}
	
	@Override
	public void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

}
