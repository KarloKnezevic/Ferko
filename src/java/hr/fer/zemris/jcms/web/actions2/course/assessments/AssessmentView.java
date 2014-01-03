package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.beans.ext.AssessmentViewBean;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsStudentViewService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

@WebClass(dataClass=AssessmentViewData.class)
public class AssessmentView extends Ext2ActionSupport<AssessmentViewData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult="popup",struts2Result="popup",registerDelayedMessages=false)}
	)
    public String execute() throws Exception {
    	AssessmentsStudentViewService.prepareStudentView(getEntityManager(), data);
    	if(data.isImposter() && AbstractActionData.RESULT_SUCCESS.equals(data.getResult())) {
    		data.setResult("popup");
    	}
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult="success", struts2Result="successStaff")
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="successStaff",navigBuilder=MainBuilder.class,navigBuilderIsRoot=true)
		}
	)
    public String staff() throws Exception {
    	AssessmentsStudentViewService.prepareStaffView(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult="success", struts2Result="successStaff")
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="successStaff",navigBuilder=MainBuilder.class,navigBuilderIsRoot=true)
		}
	)
    public String guest() throws Exception {
    	AssessmentsStudentViewService.prepareGuestView(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

    public String getAssessmentID() {
		return data.getAssessmentID();
	}
    public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
    
    public String getUserID() {
		return data.getUserID();
	}
    public void setUserID(String userID) {
		data.setUserID(userID);
	}

	public AssessmentViewBean getBean() {
		return data.getBean();
	}

	public void setBean(AssessmentViewBean bean) {
		data.setBean(bean);
	}

	public String getAssessmentScanId() {
		return data.getAssessmentScanId();
	}

	public void setAssessmentScanId(String assessmentScanId) {
		data.setAssessmentScanId(assessmentScanId);
	}
}
