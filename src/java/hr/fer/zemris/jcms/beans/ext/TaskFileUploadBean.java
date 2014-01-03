package hr.fer.zemris.jcms.beans.ext;

public class TaskFileUploadBean extends FileUploadBean{
	
	private String fileTag;
	
	public TaskFileUploadBean() {
	}

	public String getFileTag() {
		return fileTag;
	}
	
	public void setFileTag(String fileTag) {
		this.fileTag = fileTag;
	}
}
