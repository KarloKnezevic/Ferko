package hr.fer.zemris.jcms.beans;

public class CCTAMatrixColumn {
	private boolean present;
	private Long courseComponentTaskID;
	private Long userID;

	public CCTAMatrixColumn(Long userID, Long courseComponentTaskID,
			boolean present) {
		super();
		this.userID = userID;
		this.courseComponentTaskID = courseComponentTaskID;
		this.present = present;
	}

	public boolean isPresent() {
		return present;
	}

	public Long getCourseComponentTaskID() {
		return courseComponentTaskID;
	}

	public Long getUserID() {
		return userID;
	}
}
