package hr.fer.zemris.jcms.beans.ext;

import java.io.File;

public class FileUploadBean {
	
	private File upload;
	private String uploadFileName;
	private String uploadContentType;
	
	public FileUploadBean() {
	}
	
	public File getUpload() {
		return upload;
	}
	public void setUpload(File upload) {
		this.upload = upload;
	}
	public String getUploadFileName() {
		return uploadFileName;
	}
	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	public String getUploadContentType() {
		return uploadContentType;
	}
	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}
}
