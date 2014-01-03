package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;

import java.util.List;

import com.opensymphony.xwork2.util.CreateIfNull;

public class AdminListAppealsBean {

	@CreateIfNull(value=true)
	private List<AssessmentAppealInstance> appeals;
	
	private String assessmentID;
	private String courseInstanceID;

	public AdminListAppealsBean() {
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

	/**
	 * @return Lista žalbi za određenu provjeru.
	 */
	public List<AssessmentAppealInstance> getAppeals() {
		return this.appeals;
	}

	/**
	 * @param appeals the appeals to set
	 */
	public void setAppeals(List<AssessmentAppealInstance> appeals) {
		this.appeals = appeals;
	}
}
