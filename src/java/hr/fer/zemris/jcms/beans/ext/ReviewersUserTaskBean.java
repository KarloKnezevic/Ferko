package hr.fer.zemris.jcms.beans.ext;

public class ReviewersUserTaskBean extends BaseUserBean {
	
	private String assignmentID;
	private String locked;
	private String reviewed;
	
	public ReviewersUserTaskBean() {
	}
	
	public String getAssignmentID() {
		return assignmentID;
	}
	public void setAssignmentID(String assignmentID) {
		this.assignmentID = assignmentID;
	}
	public String getReviewed() {
		return reviewed;
	}
	public void setReviewed(String reviewed) {
		this.reviewed = reviewed;
	}
	
	public String getLocked() {
		return locked;
	}
	public void setLocked(String locked) {
		this.locked = locked;
	}
}
