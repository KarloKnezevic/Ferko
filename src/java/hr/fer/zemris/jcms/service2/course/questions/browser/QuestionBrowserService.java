package hr.fer.zemris.jcms.service2.course.questions.browser;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.questions.QuestionGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AddQuestionGroupData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class QuestionBrowserService {

public static void prepareNewGroup(EntityManager em, AddQuestionGroupData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canUseQuestionBrowser(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.getBean().setIsvuCode(data.getCourseInstance().getCourse().getIsvuCode());
		data.setResult(AbstractActionData.RESULT_INPUT);		
	}

	public static void prepareGroupEdit(EntityManager em, AddQuestionGroupData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canUseQuestionBrowser(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		QuestionGroup qg = DAOHelperFactory.getDAOHelper().getQuestionsDAO().getQuestionGroup(em, data.getQuestionGroupID());
		if(qg == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if(!qg.getCourse().getIsvuCode().equals(data.getCourseInstance().getCourse().getIsvuCode())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.getBean().setName(qg.getName());
		// TODO: podrska za tagove
		data.getBean().setId(qg.getId());
		data.setResult(AbstractActionData.RESULT_INPUT);		
	}
	
	public static void saveOrUpdateGroup(EntityManager em, AddQuestionGroupData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canUseQuestionBrowser(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		boolean hasErrors = false;
		
		if(StringUtil.isStringBlank(data.getBean().getName())) {
			hasErrors = true;
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.emptyQGroupName"));
		}
		
		if(hasErrors) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		QuestionGroup qg = null;
		if(data.getBean().getId() == null) {
			qg = new QuestionGroup();
			qg.setCourse(data.getCourseInstance().getCourse());
			qg.setName(data.getBean().getName());
			// TODO: tagovi
			DAOHelperFactory.getDAOHelper().getQuestionsDAO().save(em, qg);
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("QuestionGroup.info.added"));
		} else {

			qg = DAOHelperFactory.getDAOHelper().getQuestionsDAO().getQuestionGroup(em, data.getBean().getId());
			if(qg == null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			
			if(!qg.getCourse().getIsvuCode().equals(data.getCourseInstance().getCourse().getIsvuCode())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			
			qg.setName(data.getBean().getName());
			// TODO: podrska za tagove
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("QuestionGroup.info.updated"));
		}

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
}
