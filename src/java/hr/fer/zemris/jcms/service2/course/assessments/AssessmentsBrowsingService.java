package hr.fer.zemris.jcms.service2.course.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.beans.AssessmentConfigurationSelectorBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.service2.course.CourseServiceUtil;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentListData;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

/**
 * Sloj usluge koji nudi općenito navigiranje kroz provjere, zastavice i slično.
 * 
 * @author marcupic
 *
 */
public class AssessmentsBrowsingService {

	/* ====================================================================================
	 * 
	 * RAD S PROVJERAMA - glavne metode
	 * 
	 * ====================================================================================
	 */
	
	/**
	 * Metoda dohvaća sve provjere i zastavice i prikazuje ih.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void showAdminList(EntityManager em, AdminAssessmentListData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// Dohvati popis grupa studenata
		List<Group> groupsToDisplay = CourseServiceUtil.filterAccesibleLectureGroups(em, data.getCurrentUser(), data.getCourseInstance(), true);
		data.setGroupsToDisplay(groupsToDisplay);
		if(!groupsToDisplay.isEmpty()) {
			data.setSelectedGroup(groupsToDisplay.get(0));
		}
		
		// Ima li dozvole za management?
		data.setCanManageAssessments(JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance()));
		
		// Dohvati provjere i zastavice
		data.setAssessments(AssessmentsUtil.getSortedCourseInstanceAssessments(em, data.getCourseInstance()));
		data.setAssessmentFlags(AssessmentsUtil.getSortedCourseInstanceAssessmentFlags(em, data.getCourseInstance()));
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda dohvaća i prikazuje jednu konkretnu provjeru.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentView(EntityManager em, AdminAssessmentViewData data) {
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		Assessment assessment = data.getAssessment();

		// Dozvole
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		// Pripremi podatke
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, assessment, data.getCurrentUser());
		List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, assessment);
		Collections.sort(myFiles);
		Collections.sort(aFiles);
		List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(myFiles.size()+aFiles.size());
		allFiles.addAll(aFiles);
		allFiles.addAll(myFiles);
		data.setFiles(allFiles);
		data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration()));
		data.setConfSelectors(AssessmentService.getAllConfigurationSelectors(assessment));
		if(assessment.getAssessmentConfiguration()!=null && !data.getConfSelectors().isEmpty()) {
			data.getConfSelectors().add(0, new AssessmentConfigurationSelectorBean("-","--------"));
		}
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
}
