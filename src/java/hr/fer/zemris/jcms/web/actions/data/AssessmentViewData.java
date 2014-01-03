package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.AssessmentViewBean;

import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserSpecificEvent;
import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.assessments.AssessmentView;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AssessmentView}.
 *  
 * @author marcupic
 *
 */
public class AssessmentViewData extends BaseAssessment {
	
	protected String courseInstanceID;
	protected String assessmentID;
	protected AssessmentViewBean bean = new AssessmentViewBean();
	protected String userID;
	protected String assessmentScanId;
	
	protected String assessmentConfigurationKey;
	protected List<AssessmentFile> files;
	protected AssessmentScore score;
	protected AssessmentFlagValue flagValue;
	protected Object details;
	protected boolean canTake;
	protected String[] problemsIds;
	protected List<AssessmentAppealInstance> userAppeals;
	protected String[] answers;

	protected UserSpecificEvent userSpecificEvent;

	protected boolean imposter;
	protected User student;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public User getStudent() {
		return student;
	}
	public void setStudent(User student) {
		this.student = student;
	}
	
	public boolean isImposter() {
		return imposter;
	}
	public void setImposter(boolean imposter) {
		this.imposter = imposter;
	}
	
	public UserSpecificEvent getUserSpecificEvent() {
		return userSpecificEvent;
	}
	public void setUserSpecificEvent(UserSpecificEvent userSpecificEvent) {
		this.userSpecificEvent = userSpecificEvent;
	}
	
	public String getProblemsIds(int index) {
		return this.problemsIds[index]; 
	}
	
	public String getAssessmentConfigurationKey() {
		return assessmentConfigurationKey;
	}
	public void setAssessmentConfigurationKey(String assessmentConfigurationKey) {
		this.assessmentConfigurationKey = assessmentConfigurationKey;
	}

	public List<AssessmentFile> getFiles() {
		return files;
	}
	public void setFiles(List<AssessmentFile> files) {
		this.files = files;
	}
	
	public AssessmentScore getScore() {
		return score;
	}
	public void setScore(AssessmentScore score) {
		this.score = score;
	}

	public Object getDetails() {
		return details;
	}
	public void setDetails(Object details) {
		this.details = details;
	}
	
	public AssessmentFlagValue getFlagValue() {
		return flagValue;
	}
	public void setFlagValue(AssessmentFlagValue flagValue) {
		this.flagValue = flagValue;
	}
	
	public boolean getCanTake() {
		return canTake;
	}
	public void setCanTake(boolean canTake) {
		this.canTake = canTake;
	}

	public String[] getProblemsIds() {
		return problemsIds;
	}

	public void setProblemsIds(String[] problemsIds) {
		this.problemsIds = problemsIds;
	}

	public List<AssessmentAppealInstance> getUserAppeals() {
		return userAppeals;
	}

	public void setUserAppeals(List<AssessmentAppealInstance> userAppeals) {
		this.userAppeals = userAppeals;
	}

	public String[] getAnswers() {
		return answers;
	}

	public void setAnswers(String[] answers) {
		this.answers = answers;
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
    
    public String getUserID() {
		return userID;
	}
    public void setUserID(String userID) {
		this.userID = userID;
	}

	public AssessmentViewBean getBean() {
		return bean;
	}

	public void setBean(AssessmentViewBean bean) {
		this.bean = bean;
	}

	public String getAssessmentScanId() {
		return assessmentScanId;
	}

	public void setAssessmentScanId(String assessmentScanId) {
		this.assessmentScanId = assessmentScanId;
	}

}
