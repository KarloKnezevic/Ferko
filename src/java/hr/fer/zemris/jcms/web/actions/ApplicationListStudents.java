package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationListStudentsData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class ApplicationListStudents extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private Long definitionID;
	private ApplicationListStudentsData data = null;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ApplicationListStudentsData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationListStudentsData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getDefinitionID());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
    public Long getDefinitionID() {
		return definitionID;
	}

	public void setDefinitionID(Long definitionID) {
		this.definitionID = definitionID;
	}
    
	public ApplicationListStudentsData getData() {
		return data;
	}

	public void setData(ApplicationListStudentsData data) {
		this.data = data;
	}
}
