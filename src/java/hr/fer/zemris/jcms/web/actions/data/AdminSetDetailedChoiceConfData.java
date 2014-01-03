package hr.fer.zemris.jcms.web.actions.data;

import java.io.File;

import java.util.List;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminSetDetailedChoiceConf}.
 * 
 * @author Ivan Krišto
 * 
 */
public class AdminSetDetailedChoiceConfData extends BaseAssessment {

	private String assessmentID;
	private String errorColumnText;
	private String selectedView = "default";
	private String problemsNum;
	private String groupsNum;
	private String answersNumber;
	private boolean errorColumn;
	private boolean personalizedGroups;

	private String scoreCorrect;
	private String scoreIncorrect;
	private String scoreUnanswered;
	private String detailTaskScores;

	private File dataFile;
	private String dataFileContentType;
	private String dataFileFileName;

	private String correctAnswers;

	private String groupsLabels;
	private String intervalStart;
	private String intervalEnd;

	private String problemsLabels;

	private String mapping;

	private String problemManipulators;

	private List<Assessment> availableAssessments;
	
	/**
	 * Konstruktor.
	 * 
	 * @param messageLogger
	 *            lokalizirane poruke
	 */
	public AdminSetDetailedChoiceConfData(IMessageLogger messageLogger) {
		super(messageLogger);
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

	public String getSelectedView() {
		return selectedView;
	}

	public void setSelectedView(String selectedView) {
		this.selectedView = selectedView;
	}

	public String getProblemsNum() {
		return problemsNum;
	}

	public void setProblemsNum(String problemsNum) {
		this.problemsNum = problemsNum;
	}

	public String getGroupsNum() {
		return groupsNum;
	}

	public void setGroupsNum(String groupsNum) {
		this.groupsNum = groupsNum;
	}

	public String getAnswersNumber() {
		return answersNumber;
	}

	public void setAnswersNumber(String answersNumber) {
		this.answersNumber = answersNumber;
	}

	public boolean getErrorColumn() {
		return errorColumn;
	}

	public void setErrorColumn(boolean errorColumn) {
		this.errorColumn = errorColumn;
	}

	public boolean getPersonalizedGroups() {
		return personalizedGroups;
	}

	public void setPersonalizedGroups(boolean personalizedGroups) {
		this.personalizedGroups = personalizedGroups;
	}

	public String getScoreCorrect() {
		return scoreCorrect;
	}

	public void setScoreCorrect(String scoreCorrect) {
		this.scoreCorrect = scoreCorrect;
	}

	public String getScoreIncorrect() {
		return scoreIncorrect;
	}

	public void setScoreIncorrect(String scoreIncorrect) {
		this.scoreIncorrect = scoreIncorrect;
	}

	public String getScoreUnanswered() {
		return scoreUnanswered;
	}

	public void setScoreUnanswered(String scoreUnanswered) {
		this.scoreUnanswered = scoreUnanswered;
	}

	public String getDetailTaskScores() {
		return detailTaskScores;
	}

	public void setDetailTaskScores(String detailTaskScores) {
		this.detailTaskScores = detailTaskScores;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	public String getDataFileContentType() {
		return dataFileContentType;
	}

	public void setDataFileContentType(String dataFileContentType) {
		this.dataFileContentType = dataFileContentType;
	}

	public String getDataFileFileName() {
		return dataFileFileName;
	}

	public void setDataFileFileName(String dataFileFileName) {
		this.dataFileFileName = dataFileFileName;
	}

	public String getCorrectAnswers() {
		return correctAnswers;
	}

	public void setCorrectAnswers(String correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	public void setGroupsLabels(String groupsLabels) {
		this.groupsLabels = groupsLabels;
	}

	public String getGroupsLabels() {
		return groupsLabels;
	}

	public void setIntervalStart(String intervalStart) {
		this.intervalStart = intervalStart;
	}

	public String getIntervalStart() {
		return intervalStart;
	}

	public void setIntervalEnd(String intervalEnd) {
		this.intervalEnd = intervalEnd;
	}

	public String getIntervalEnd() {
		return intervalEnd;
	}

	public void setProblemsLabels(String problemsLabels) {
		this.problemsLabels = problemsLabels;
	}

	public String getProblemsLabels() {
		return problemsLabels;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public String getProblemManipulators() {
		return problemManipulators;
	}

	public void setProblemManipulators(String problemManipulators) {
		this.problemManipulators = problemManipulators;
	}
	
	public List<Assessment> getAvailableAssessments() {
		return availableAssessments;
	}
	public void setAvailableAssessments(List<Assessment> availableAssessments) {
		this.availableAssessments = availableAssessments;
	}
	
}
