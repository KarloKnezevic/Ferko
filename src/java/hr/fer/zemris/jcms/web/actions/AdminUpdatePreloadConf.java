package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ConfPreloadBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminUpdatePreloadConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AdminUpdatePreloadConf extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String assessmentID;
	private AdminUpdatePreloadConfData data = null;
	private ConfPreloadBean bean = new ConfPreloadBean();
	
    public String update() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AdminUpdatePreloadConfData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAdminUpdatePreloadConfData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID());
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
    
    public AdminUpdatePreloadConfData getData() {
		return data;
	}
    public void setData(AdminUpdatePreloadConfData data) {
		this.data = data;
	}
    
    public ConfPreloadBean getBean() {
		return bean;
	}
    public void setBean(ConfPreloadBean bean) {
		this.bean = bean;
	}
}
