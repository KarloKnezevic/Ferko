package hr.fer.zemris.jcms.service.assessments;

public class StudentFlag {

	private Long assessmentFlagID;
	private Long userID;
	private boolean value;
	private boolean errorOccured;
	
	public StudentFlag(Long assessmentFlagID, Long userID, boolean value, boolean errorOccured) {
		super();
		this.assessmentFlagID = assessmentFlagID;
		this.userID = userID;
		this.value = value;
		this.errorOccured = errorOccured;
	}

	public Long getAssessmentFlagID() {
		return assessmentFlagID;
	}

	public Long getUserID() {
		return userID;
	}

	public boolean getValue() {
		return value;
	}
	
	public boolean hasErrorOccured() {
		return errorOccured;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("score[uid=").append(userID).append(",")
		  .append("afid=").append(assessmentFlagID).append(",")
		  .append("value=").append(value).append(",")
		  .append("error=").append(errorOccured)
		  .append("]");
		return sb.toString();
	}

}
