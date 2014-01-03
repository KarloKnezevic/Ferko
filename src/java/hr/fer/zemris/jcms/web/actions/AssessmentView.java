package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.AssessmentViewBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class AssessmentView extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String assessmentID;
	private AssessmentViewData data = null;
	private AssessmentViewBean bean = null;
	private String userID;
	protected String assessmentScanId;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentViewData(MessageLoggerFactory.createMessageLogger(this, true));
		bean = new AssessmentViewBean();
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getAssessmentViewData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getUserID());
    	} else {
    		return NO_PERMISSION;
    	}
        return SUCCESS;
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
    
    public AssessmentViewData getData() {
		return data;
	}
    public void setData(AssessmentViewData data) {
		this.data = data;
	}
    
    public String getUserID() {
		return userID;
	}
    public void setUserID(String userID) {
		this.userID = userID;
	}

	public AssessmentViewBean getBean() {
		return bean;
	}

	public void setBean(AssessmentViewBean bean) {
		this.bean = bean;
	}

	public String getAssessmentScanId() {
		return assessmentScanId;
	}

	public void setAssessmentScanId(String assessmentScanId) {
		this.assessmentScanId = assessmentScanId;
	}
}
