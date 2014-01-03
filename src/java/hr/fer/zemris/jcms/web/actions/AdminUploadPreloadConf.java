package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminUploadPreloadConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AdminUploadPreloadConf extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String assessmentID;
	private String text;
	private String appendOrReplace;
	private AdminUploadPreloadConfData data = null;
	
    public String upload() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AdminUploadPreloadConfData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAdminUploadPreloadConfData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getText(), getAppendOrReplace());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			data.getMessageLogger().registerAsDelayed();
			return INPUT;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
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
    
    public AdminUploadPreloadConfData getData() {
		return data;
	}
    public void setData(AdminUploadPreloadConfData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAppendOrReplace() {
		return appendOrReplace;
	}

	public void setAppendOrReplace(String appendOrReplace) {
		this.appendOrReplace = appendOrReplace;
	}
}
