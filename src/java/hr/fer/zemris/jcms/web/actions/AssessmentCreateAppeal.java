package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.appeals.AppealProblemType;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AssessmentCreateAppealData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Akcija za postavljanje parametara AssessmentConfProblems provjere.
 * 
 * @author Ivan Krišto
 */
public class AssessmentCreateAppeal extends ExtendedActionSupport {
	
	private static final long serialVersionUID = 2L;
	
	private String courseInstanceID;
	private String assessmentID;
	private String problemNumber;
	private String score;
	private String comment;
	private String answer; 
	private AssessmentCreateAppealData data = null;
	
	/**
	 * Postavljanje broja zadataka.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {
		return NO_PERMISSION;
	}
	
	public String badScan() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.BAD_SCAN_OFFER_SOLUTION,
														new String[] {getProblemNumber(), getAnswer()});
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String checkScore() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.CHECK_SCORE_FOR_PROBLEM,
														new String[] {getProblemNumber()});
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String wrongSolution() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.WRONG_OFFICIAL_SOLUTION,
														new String[] {getProblemNumber()});
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String newScore() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.SET_SCORE_FOR_PROBLEM,
														new String[] {getProblemNumber(), getScore()});
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String notEvaluated() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.PROBLEM_NOT_EVALUATED,
														new String[] {getProblemNumber()});
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String notProcessed() throws Exception {
		// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentCreateAppealData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getAssessmentCreateAppealData(	data, getCurrentUser().getUserID(),
														getCourseInstanceID(), getAssessmentID(),
														AppealProblemType.NOT_PROCESSED,
														null);
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

	public String getProblemNumber() {
		return this.problemNumber;
	}

	public void setProblemNumber(String problemNumber) {
		this.problemNumber = problemNumber;
	}

	/**
	 * @return the score
	 */
	public String getScore() {
		return this.score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

	/**
	 * @return the data
	 */
	public AssessmentCreateAppealData getData() {
		return this.data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(AssessmentCreateAppealData data) {
		this.data = data;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

}
