package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.User;


import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsStudentViewService.TreeRenderingClues;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.assessments.AssessmentSummaryView;
import hr.fer.zemris.util.Tree;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AssessmentSummaryView}.
 *  
 * @author marcupic
 *
 */
public class AssessmentSummaryViewData extends BaseCourseInstance {

	private String courseInstanceID;
	private boolean imposter;
	private Tree<AssessmentScore,?> scoreTree;
	private List<AssessmentScore> score;
	private List<AssessmentFlagValue> flagValues;
	
	private String dependenciesJSON;
	private List<TreeRenderingClues> renderingClues;
	
	private Grade grade;

	private Long studentID;
	private User student;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentSummaryViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public void setStudent(User student) {
		this.student = student;
	}
	public User getStudent() {
		return student;
	}
	
	public boolean isImposter() {
		return imposter;
	}
	public void setImposter(boolean imposter) {
		this.imposter = imposter;
	}
	
	public Long getStudentID() {
		return studentID;
	}
	public void setStudentID(Long studentID) {
		this.studentID = studentID;
	}
	
	public List<AssessmentScore> getScore() {
		return score;
	}
	public void setScore(List<AssessmentScore> score) {
		this.score = score;
	}
	
	public List<AssessmentFlagValue> getFlagValues() {
		return flagValues;
	}
	public void setFlagValues(List<AssessmentFlagValue> flagValues) {
		this.flagValues = flagValues;
	}
	
	public Tree<AssessmentScore,?> getScoreTree() {
		return scoreTree;
	}
	public void setScoreTree(Tree<AssessmentScore,?> scoreTree) {
		this.scoreTree = scoreTree;
	}
	
	public String getDependenciesJSON() {
		return dependenciesJSON;
	}
	public void setDependenciesJSON(String dependenciesJSON) {
		this.dependenciesJSON = dependenciesJSON;
	}
	
	public List<TreeRenderingClues> getRenderingClues() {
		return renderingClues;
	}
	public void setRenderingClues(
			List<TreeRenderingClues> renderingClues) {
		this.renderingClues = renderingClues;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public Grade getGrade() {
		return grade;
	}
	public void setGrade(Grade grade) {
		this.grade = grade;
	}
}
