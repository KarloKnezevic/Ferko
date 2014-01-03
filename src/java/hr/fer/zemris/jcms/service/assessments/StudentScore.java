package hr.fer.zemris.jcms.service.assessments;

public class StudentScore {

	private Long assessmentID;
	private Long userID;
	private boolean present;
	private double score;
	private String date;
	private AssessmentStatus status;
	private boolean errorOccured;
	private double effectiveScore;
	private boolean effectivePresent;
	private AssessmentStatus effectiveStatus;
	
	public StudentScore(Long assessmentID, Long userID, boolean present, 
			double score, AssessmentStatus status, String date, boolean errorOccured,
			double effectiveScore, boolean effectivePresent, AssessmentStatus effectiveStatus
			) {
		super();
		this.assessmentID = assessmentID;
		this.date = date;
		this.present = present;
		this.score = score;
		this.status = status;
		this.userID = userID;
		this.errorOccured = errorOccured;
		this.effectivePresent = effectivePresent;
		this.effectiveScore = effectiveScore;
		this.effectiveStatus = effectiveStatus;
	}

	public Long getAssessmentID() {
		return assessmentID;
	}

	public Long getUserID() {
		return userID;
	}

	public boolean getPresent() {
		return present;
	}

	public double getScore() {
		return score;
	}

	public String getDate() {
		return date;
	}

	public AssessmentStatus getStatus() {
		return status;
	}
	
	public boolean hasErrorOccured() {
		return errorOccured;
	}

	
	public double getEffectiveScore() {
		return effectiveScore;
	}
	public void setEffectiveScore(double effectiveScore) {
		this.effectiveScore = effectiveScore;
	}

	public boolean getEffectivePresent() {
		return effectivePresent;
	}
	public void setEffectivePresent(boolean effectivePresent) {
		this.effectivePresent = effectivePresent;
	}

	public AssessmentStatus getEffectiveStatus() {
		return effectiveStatus;
	}
	public void setEffectiveStatus(AssessmentStatus effectiveStatus) {
		this.effectiveStatus = effectiveStatus;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("score[uid=").append(userID).append(",")
		  .append("aid=").append(assessmentID).append(",")
		  .append("present=").append(present).append(",")
		  .append("score=").append(score).append(",")
		  .append("status=").append(status).append(",")
		  .append("error=").append(errorOccured)
		  .append("escore=").append(effectiveScore).append(",")
		  .append("estatus=").append(effectiveStatus).append(",")
		  .append("epresent=").append(effectivePresent)
		  .append("]");
		return sb.toString();
	}
}
