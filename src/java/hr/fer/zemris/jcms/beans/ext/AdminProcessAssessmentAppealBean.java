package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;

public class AdminProcessAssessmentAppealBean extends AssessmentViewBean {
	
	private AssessmentAppealInstance appeal;
	private String assessmentID;
	private String courseInstanceID;

	public AdminProcessAssessmentAppealBean() {
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

	public void setAppeal(AssessmentAppealInstance appeal) {
		this.appeal = appeal;
	}

	public AssessmentAppealInstance getAppeal() {
		return appeal;
	}
}
