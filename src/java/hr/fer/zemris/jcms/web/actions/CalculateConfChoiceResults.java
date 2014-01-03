package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.CalculateConfChoiceResultsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Sumiranje rezultata provjere sa bodovima po zadatcima ({@link AssessmentConfProblems}
 * i {@link AssessmentConfProblemsData}). 
 * 
 * @author Ivan Krišto
 */
public class CalculateConfChoiceResults extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;
	private String courseInstanceID;
	private String assessmentID;
	private CalculateConfChoiceResultsData data = null;

    public String execute() throws Exception {
    	data = new CalculateConfChoiceResultsData(MessageLoggerFactory.createMessageLogger(this));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfChoiceCalculateResultsData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getAssessmentID());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public CalculateConfChoiceResultsData getData() {
		return data;
	}
    public void setData(CalculateConfChoiceResultsData data) {
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
