package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AssessmentChoiceInsightData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class AssessmentChoiceInsight extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String assessmentID;
	private AssessmentChoiceInsightData data = null;
	private String userID;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentChoiceInsightData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getAssessmentChoiceInsightData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID(), getUserID());
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
    
    public AssessmentChoiceInsightData getData() {
		return data;
	}
    public void setData(AssessmentChoiceInsightData data) {
		this.data = data;
	}
    
    public String getUserID() {
		return userID;
	}
    public void setUserID(String userID) {
		this.userID = userID;
	}
}
