package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.AdminProcessAssessmentAppealBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminProcessAssessmentAppealData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AdminProcessAssessmentAppeal extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private AdminProcessAssessmentAppealData data = null;
	private AdminProcessAssessmentAppealBean bean = null;
	private String courseInstanceID;
	private String assessmentID;
	private String appealID;

    public String execute() {
    	return SHOW_FATAL_MESSAGE;
    }
    
    public String approve() {
    	data = new AdminProcessAssessmentAppealData(MessageLoggerFactory.createMessageLogger(this));
    	bean = new AdminProcessAssessmentAppealBean();
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminProcessAssessmentAppealData(	data, bean, getCurrentUser().getUserID(),
															getAppealID(), getCourseInstanceID(), "approve");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }
    
    public String lock() {
    	data = new AdminProcessAssessmentAppealData(MessageLoggerFactory.createMessageLogger(this));
    	bean = new AdminProcessAssessmentAppealBean();
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminProcessAssessmentAppealData(	data, bean, getCurrentUser().getUserID(),
															getAppealID(), getCourseInstanceID(), "lock");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }
    
    public String unlock() {
    	data = new AdminProcessAssessmentAppealData(MessageLoggerFactory.createMessageLogger(this));
    	bean = new AdminProcessAssessmentAppealBean();
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminProcessAssessmentAppealData(	data, bean, getCurrentUser().getUserID(),
															getAppealID(), getCourseInstanceID(), "unlock");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }
    
    public String reject() {
    	data = new AdminProcessAssessmentAppealData(MessageLoggerFactory.createMessageLogger(this));
    	bean = new AdminProcessAssessmentAppealBean();
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminProcessAssessmentAppealData(	data, bean, getCurrentUser().getUserID(),
															getAppealID(), getCourseInstanceID(), "reject");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }
    
    public AdminProcessAssessmentAppealData getData() {
		return data;
	}
    public void setData(AdminProcessAssessmentAppealData data) {
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

	public void setBean(AdminProcessAssessmentAppealBean bean) {
		this.bean = bean;
	}

	public AdminProcessAssessmentAppealBean getBean() {
		return bean;
	}

}
