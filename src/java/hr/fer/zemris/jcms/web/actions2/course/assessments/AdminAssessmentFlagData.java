package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsEditingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagDataData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentListBuilder;

/**
 * Akcija za pregled i editiranje podataka o vrijednostima zastavice.
 * 
 * @author marcupic
 */
@WebClass(dataClass=AdminAssessmentFlagDataData.class,defaultNavigBuilder=AdminAssessmentListBuilder.class,defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.dataViewEdit"})
public class AdminAssessmentFlagData extends Ext2ActionSupport<AdminAssessmentFlagDataData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String show() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagDataShow(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return show();
    }

	@WebMethodInfo
    public String pickLetter() throws Exception {
    	return show();
    }

	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a\\f${assessmentFlagID}")
    public String save() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagDataSave(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo(
    	dataResultMappings={
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirectList", registerDelayedMessages=true),
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_CONFIRM, struts2Result="confirm", registerDelayedMessages=false)
    	}
	)
    public String resetNL() throws Exception {
		data.setConfirmed(false);
		AssessmentsEditingService.adminAssessmentFlagDataResetManual(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo(
		lockPath="ml\\ci${courseInstanceID}\\a\\f${assessmentFlagID}",
    	dataResultMappings={
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirectList", registerDelayedMessages=true),
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_CONFIRM, struts2Result="confirm", registerDelayedMessages=false)
    	}
	)
    public String reset() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagDataResetManual(getEntityManager(), data);
		return null;
    }

	public String getAssessmentFlagID() {
		return data.getAssessmentFlagID();
	}
	public void setAssessmentFlagID(String assessmentFlagID) {
		data.setAssessmentFlagID(assessmentFlagID);
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

}
