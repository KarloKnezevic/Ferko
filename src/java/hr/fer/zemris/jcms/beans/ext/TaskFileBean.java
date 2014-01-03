package hr.fer.zemris.jcms.beans.ext;

public class TaskFileBean extends FileBean{
	private String tag;
	private String uploadDate;
	
	public String getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}

	public TaskFileBean() {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
