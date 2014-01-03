package hr.fer.zemris.jcms.beans.ext;

public class TaskReviewBean {
	
	private String extension;
	private String comment;
	private String passed;
	private String score;
	private String reviewed;
	
	public TaskReviewBean() {
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getPassed() {
		return passed;
	}

	public void setPassed(String passed) {
		this.passed = passed;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getReviewed() {
		return reviewed;
	}

	public void setReviewed(String reviewed) {
		this.reviewed = reviewed;
	}
}
