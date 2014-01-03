package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.AssessmentConfExternal;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.FetchConfExternalResultsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Dohvat bodova dobivenih na vanjskoj provjeri ({@link AssessmentConfExternal}. 
 * 
 * @author marcupic
 */
public class FetchConfExternalResults extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;
	private String courseInstanceID;
	private String assessmentID;
	private FetchConfExternalResultsData data = null;

    public String execute() throws Exception {
    	data = new FetchConfExternalResultsData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getFetchConfExternalResultsData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public FetchConfExternalResultsData getData() {
		return data;
	}
    public void setData(FetchConfExternalResultsData data) {
		this.data = data;
	}

	public String getAssessmentID() {
		return this.assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public String getCourseInstanceID() {
		return this.courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
