package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentConfSelectData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AdminAssessmentConfSelect extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private AdminAssessmentConfSelectData data = null;
	private String courseInstanceID;
	private String assessmentID;
	private String confSelectorID;
	
    public String askConfirm() throws Exception {
    	data = new AdminAssessmentConfSelectData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminAssessmentConfSelectData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getConfSelectorID(), "askConfirm");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
	}

    public String doIt() throws Exception {
    	data = new AdminAssessmentConfSelectData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminAssessmentConfSelectData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getConfSelectorID(), "doIt");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String changeIt() throws Exception {
    	data = new AdminAssessmentConfSelectData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminAssessmentConfSelectData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getConfSelectorID(), "changeIt");
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() {
		return NO_PERMISSION;
    }
    
    public AdminAssessmentConfSelectData getData() {
		return data;
	}
    public void setData(AdminAssessmentConfSelectData data) {
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

	public String getConfSelectorID() {
		return confSelectorID;
	}

	public void setConfSelectorID(String confSelectorID) {
		this.confSelectorID = confSelectorID;
	}
    
    
}
