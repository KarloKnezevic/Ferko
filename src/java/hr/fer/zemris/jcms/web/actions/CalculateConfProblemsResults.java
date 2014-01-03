package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.CalculateConfProblemsResultsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Sumiranje rezultata provjere sa bodovima po zadatcima ({@link AssessmentConfProblems}
 * i {@link AssessmentConfProblemsData}). 
 * 
 * @author Ivan Krišto
 */
public class CalculateConfProblemsResults extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;
	private String courseInstanceID;
	private String assessmentID;
	private CalculateConfProblemsResultsData data = null;

    public String execute() throws Exception {
    	data = new CalculateConfProblemsResultsData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfProblemsCalculateResultsData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public CalculateConfProblemsResultsData getData() {
		return data;
	}
    public void setData(CalculateConfProblemsResultsData data) {
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
