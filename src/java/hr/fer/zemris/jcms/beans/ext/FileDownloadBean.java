package hr.fer.zemris.jcms.beans.ext;

import java.io.File;
import java.io.InputStream;

public class FileDownloadBean {
	private File file;
	private String fileName;
	private String mimeType;
	private InputStream stream;
	private long length;
	
	public FileDownloadBean() {
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public InputStream getStream() {
		return stream;
	}
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
