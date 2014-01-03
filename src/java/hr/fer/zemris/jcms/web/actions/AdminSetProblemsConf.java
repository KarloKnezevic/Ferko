package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminSetProblemsConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Akcija za postavljanje parametara AssessmentConfProblems provjere.
 * 
 * @author Ivan Krišto
 */
public class AdminSetProblemsConf extends ExtendedActionSupport {
	
	private static final long serialVersionUID = 2L;
	
	private String courseInstanceID;
	private String assessmentID;
	private String numberOfProblems;
	private AdminSetProblemsConfData data = null;
	
	/**
	 * Postavljanje broja zadataka.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String confNumberOfProblems() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AdminSetProblemsConfData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAdminSetProblemsConfData(		data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														"numberOfProblems", getNumberOfProblems());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
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

	public AdminSetProblemsConfData getData() {
		return this.data;
	}

	public void setData(AdminSetProblemsConfData data) {
		this.data = data;
	}

	public String getNumberOfProblems() {
		return this.numberOfProblems;
	}

	public void setNumberOfProblems(String numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}

}
