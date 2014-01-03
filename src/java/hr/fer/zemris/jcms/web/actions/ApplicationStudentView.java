package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class ApplicationStudentView extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private Long applicationID;
	private ApplicationStudentViewData data = null;
	
	@Override
	public void prepare() throws Exception {
		data = new ApplicationStudentViewData(MessageLoggerFactory.createMessageLogger(this, true));
		for(ApplicationStatus as : ApplicationStatus.values()){
			data.getStatuses().put(as, getText(as.name()));
		}
	}
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		ApplicationService.getApplicationStudentViewData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getApplicationID());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public Long getApplicationID() {
		return applicationID;
	}
    public void setApplicationID(Long applicationID) {
		this.applicationID = applicationID;
	}
    
    public ApplicationStudentViewData getData() {
		return data;
	}
    public void setData(ApplicationStudentViewData data) {
		this.data = data;
	}
}
