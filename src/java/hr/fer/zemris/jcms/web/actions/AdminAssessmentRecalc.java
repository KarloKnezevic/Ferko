package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentRecalcData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class AdminAssessmentRecalc extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private AdminAssessmentRecalcData data = null;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AdminAssessmentRecalcData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getAdminAssessmentRecalcData(data, getCurrentUser().getUserID(), getCourseInstanceID());
    		data.getMessageLogger().registerAsDelayed();
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public AdminAssessmentRecalcData getData() {
		return data;
	}
    
    public void setData(AdminAssessmentRecalcData data) {
		this.data = data;
	}
}
