package hr.fer.zemris.jcms.statistics.assessments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssessmentStatistics implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private String courseName;
	private Long assessmentID;
	private String assessmentName;
	private List<StatisticsName> availableStatistics;
	
	public AssessmentStatistics() {
		availableStatistics = new ArrayList<StatisticsName>();
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Long getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(Long assessmentID) {
		this.assessmentID = assessmentID;
	}

	public String getAssessmentName() {
		return assessmentName;
	}

	public void setAssessmentName(String assessmentName) {
		this.assessmentName = assessmentName;
	}

	public List<StatisticsName> getAvailableStatistics() {
		return availableStatistics;
	}

	public void setAvailableStatistics(List<StatisticsName> availableStatistics) {
		this.availableStatistics = availableStatistics;
	}
}
