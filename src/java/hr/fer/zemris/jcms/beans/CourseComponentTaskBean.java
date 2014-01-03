package hr.fer.zemris.jcms.beans;


public class CourseComponentTaskBean {
	private String id;
	private String title;
	private String description;
	private String deadline;
	private String filesRequiredCount;
	private String fileTags;
	private String maxFileSize;
	private String maxFilesCount;
	private String needsReviewers;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getFilesRequiredCount() {
		return filesRequiredCount;
	}
	public void setFilesRequiredCount(String filesRequiredCount) {
		this.filesRequiredCount = filesRequiredCount;
	}
	public String getFileTags() {
		return fileTags;
	}
	public void setFileTags(String fileTags) {
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
	public String getNeedsReviewers() {
		return needsReviewers;
	}
	public void setNeedsReviewers(String needsReviewers) {
		this.needsReviewers = needsReviewers;
	}
}
