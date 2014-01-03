package hr.fer.zemris.jcms.web.actions2.course.issues;

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.course.issues.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;

@WebClass(dataClass=IssuesData.class)
public class IssuesInitialization extends Ext2ActionSupport<IssuesData> {
	
	private static final long serialVersionUID = 2L;
	
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS, navigBuilder=DefaultNavigationBuilder.class)})

	public String execute() throws Exception {

			EntityManager em = getEntityManager();
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			IJCMSSecurityManager securityManager = JCMSSecurityManagerFactory.getManager();
			securityManager.init(data.getCurrentUser(), em);
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			
			if (!securityManager.canPerformSystemAdministration()){
				data.getMessageLogger().addInfoMessage(getText("ITS.initUnsuccessful"));
				return null;
			}
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
			return null;
		}
	

}
