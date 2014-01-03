package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationMainData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ApplicationMain extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private ApplicationMainData data = null;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ApplicationMainData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationMainData(data, getCurrentUser().getUserID(), getCourseInstanceID());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public ApplicationMainData getData() {
		return data;
	}

	public void setData(ApplicationMainData data) {
		this.data = data;
	}
    

}
