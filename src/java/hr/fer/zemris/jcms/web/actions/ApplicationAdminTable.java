package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminTableData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ApplicationAdminTable extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private ApplicationAdminTableData data = new ApplicationAdminTableData(MessageLoggerFactory.createMessageLogger(this, true));
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationAdminTableData(data, getCurrentUser().getUserID(), getCourseInstanceID());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public ApplicationAdminTableData getData() {
		return data;
	}

	public void setData(ApplicationAdminTableData data) {
		this.data = data;
	}
    

}
