package hr.fer.zemris.jcms.beans.ext;

import java.util.List;

import com.opensymphony.xwork2.util.CreateIfNull;

public class ConfChoiceScoreEditBean {

	private Character letter;

	@CreateIfNull(value=true)
	private List<ConfChoiceScoreBean> items;
	
	private String assessmentID;
	private String courseInstanceID;
	private int problemsNum;

	public ConfChoiceScoreEditBean() {
	}
	
	public Character getLetter() {
		return letter;
	}
	public void setLetter(Character letter) {
		this.letter = letter;
	}
	public List<ConfChoiceScoreBean> getItems() {
		return items;
	}
	public void setItems(List<ConfChoiceScoreBean> items) {
		this.items = items;
	}
	public String getAssessmentID() {
		return assessmentID;
	}
	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public int getProblemsNum() {
		return this.problemsNum;
	}

	public void setProblemsNum(int problemsNum) {
		this.problemsNum = problemsNum;
	}
}
