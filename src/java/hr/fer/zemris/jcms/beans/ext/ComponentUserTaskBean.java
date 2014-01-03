package hr.fer.zemris.jcms.beans.ext;


import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ComponentUserTaskBean {
	private String taskId;
	private String assignmentID;
	private String title;
	private String description;
	private String deadline;
	private Map<String,String> fileTags;
	private String maxFileSize;
	private String maxFilesCount;
	private String filesRequiredCount;
	private boolean locked;
	private String lockingDate;
	private String extensionDate;
	private boolean reviewed;
	private String reviewedBy;
	private String comment;
	private boolean passed;
	private String score;
	private List<TaskFileBean> taskUploadList = Collections.emptyList();
	
	public ComponentUserTaskBean() {
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getAssignmentID() {
		return assignmentID;
	}

	public void setAssignmentID(String assignmentID) {
		this.assignmentID = assignmentID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public Map<String, String> getFileTags() {
		return fileTags;
	}

	public void setFileTags(Map<String, String> fileTags) {
		this.fileTags = fileTags;
	}

	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public String getMaxFilesCount() {
		return maxFilesCount;
	}

	public void setMaxFilesCount(String maxFilesCount) {
		this.maxFilesCount = maxFilesCount;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getLockingDate() {
		return lockingDate;
	}

	public void setLockingDate(String lockingDate) {
		this.lockingDate = lockingDate;
	}

	public String getExtensionDate() {
		return extensionDate;
	}

	public void setExtensionDate(String extensionDate) {
		this.extensionDate = extensionDate;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public String getReviewedBy() {
		return reviewedBy;
	}

	public void setReviewedBy(String reviewedBy) {
		this.reviewedBy = reviewedBy;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public List<TaskFileBean> getTaskUploadList() {
		return taskUploadList;
	}

	public void setTaskUploadList(List<TaskFileBean> taskUploadList) {
		this.taskUploadList = taskUploadList;
	}

	public String getFilesRequiredCount() {
		return filesRequiredCount;
	}

	public void setFilesRequiredCount(String filesRequiredCount) {
		this.filesRequiredCount = filesRequiredCount;
	}
}
