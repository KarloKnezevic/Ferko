package hr.fer.zemris.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DeleteOnCloseFileInputStream extends InputStream {

	private File file;
	private InputStream is;
	private String mimeType;
	private String fileName;
	
	public DeleteOnCloseFileInputStream(File file) throws IOException {
		is = new BufferedInputStream(new FileInputStream(file));
		this.file = file;
	}
	
	public long getLength() {
		return file.length();
	}
	
	public String getFileName() {
		if(fileName==null) return file.getName();
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		if(mimeType==null) return "application/octet-stream";
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	@Override
	public int read() throws IOException {
		return is.read();
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public void close() throws IOException {
		try {
			if(is!=null) is.close();
		} finally {
			if(file!=null) file.delete();
		}
	}

	@Override
	public synchronized void mark(int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return is.skip(n);
	}
}
