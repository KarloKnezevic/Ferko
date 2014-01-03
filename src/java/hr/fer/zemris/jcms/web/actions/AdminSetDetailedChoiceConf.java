package hr.fer.zemris.jcms.web.actions;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminSetDetailedChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Akcija za postavljanje opširnih podataka {@link AssessmentConfChoice} provjere.
 * 
 * @author Ivan Krišto
 */
@Deprecated
public class AdminSetDetailedChoiceConf extends ExtendedActionSupport implements Preparable {
	
	private static final long serialVersionUID = 2L;
	
	private String courseInstanceID;
	private String assessmentID;
	private String errorColumnText;
	
	private AdminSetDetailedChoiceConfData data = null;
	
	@Override
	public void prepare() throws Exception {
		data = new AdminSetDetailedChoiceConfData(MessageLoggerFactory.createMessageLogger(this));
	}
	
	public String show() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	BasicBrowsing.getAdminSetDetailedChoiceConfData(data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														getErrorColumnText(), "init");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAdminSetDetailedChoiceConfData(data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														getErrorColumnText(), "upload");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String execute() throws Exception {
		return show();
	}

	public AdminSetDetailedChoiceConfData getData() {
		return data;
	}

	public void setData(AdminSetDetailedChoiceConfData data) {
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

	public String getErrorColumnText() {
		return errorColumnText;
	}

	public void setErrorColumnText(String errorColumnText) {
		this.errorColumnText = errorColumnText;
	}
	
}
