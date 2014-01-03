package hr.fer.zemris.jcms.service.assessments;

import java.util.Date;

public class StudentTask {

	private Long userId;
	private Long taskId;
	private boolean locked;
	private Date lockingDate;
	private boolean reviewed;
	private boolean passed;
	private double score;
	
	public StudentTask(Long userId, Long taskId, boolean locked, Date lockingDate, boolean reviewed,
			boolean passed, double score) {
		super();
		this.userId = userId;
		this.taskId = taskId;
		this.locked = locked;
		this.lockingDate = lockingDate;
		this.reviewed = reviewed;
		this.passed = passed;
		this.score = score;
	}
	
	public Long getUserId() {
		return userId;
	}
	public Long getTaskId() {
		return taskId;
	}
	public boolean isLocked() {
		return locked;
	}
	public Date getLockingDate() {
		return lockingDate;
	}
	public boolean isReviewed() {
		return reviewed;
	}
	public boolean isPassed() {
		return passed;
	}
	public double getScore() {
		return score;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public void setLockingDate(Date lockingDate) {
		this.lockingDate = lockingDate;
	}
	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}
	
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		return "uid="+userId+", tid="+taskId+", locked="+locked+", reviwed="+reviewed+", passed="+passed+", score="+score+", date="+lockingDate;
	}
}
