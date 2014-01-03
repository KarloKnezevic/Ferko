package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.AdminAssessmentAppealBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentAppealData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AdminAssessmentAppeal extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private AdminAssessmentAppealData data = null;
	private AdminAssessmentAppealBean bean = null;
	private String courseInstanceID;
	private String assessmentID;
	private String appealID;

    public String execute() {
    	data = new AdminAssessmentAppealData(MessageLoggerFactory.createMessageLogger(this));
    	bean = new AdminAssessmentAppealBean();
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminAssessmentAppealData(data, bean, getCurrentUser().getUserID(), getAppealID(), getCourseInstanceID(), getAssessmentID());
		BasicBrowsing.getAssessmentViewData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), bean.getAppeal().getCreatorUser().getId().toString());
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }
    
    public AdminAssessmentAppealData getData() {
		return data;
	}
    public void setData(AdminAssessmentAppealData data) {
		this.data = data;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public String getAppealID() {
		return appealID;
	}

	public void setAppealID(String appealID) {
		this.appealID = appealID;
	}

	public void setBean(AdminAssessmentAppealBean bean) {
		this.bean = bean;
	}

	public AdminAssessmentAppealBean getBean() {
		return bean;
	}
}
