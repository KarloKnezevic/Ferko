package hr.fer.zemris.jcms.beans;

public class CCIAMatrixColumn {
	private boolean present;
	private Long courseComponentItemAssessmentID;
	private Long userID;

	public CCIAMatrixColumn(Long userID, Long courseComponentItemAssessmentID,
			boolean present) {
		super();
		this.userID = userID;
		this.courseComponentItemAssessmentID = courseComponentItemAssessmentID;
		this.present = present;
	}

	public boolean isPresent() {
		return present;
	}

	public Long getCourseComponentItemAssessmentID() {
		return courseComponentItemAssessmentID;
	}

	public Long getUserID() {
		return userID;
	}
}
