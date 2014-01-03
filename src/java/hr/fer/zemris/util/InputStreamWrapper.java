package hr.fer.zemris.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {

	private InputStream stream;
	private String mimeType;
	private String fileName;
	private long length;
	
	
	public InputStreamWrapper(InputStream stream, String fileName, long length,
			String mimeType) {
		super();
		this.stream = stream;
		this.fileName = fileName;
		this.length = length;
		this.mimeType = mimeType;
	}

	public long getLength() {
		return length;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getMimeType() {
		if(mimeType==null) return "application/octet-stream";
		return mimeType;
	}
	
	public InputStream getStream() {
		return stream;
	}
	
	public static InputStreamWrapper createInputStreamWrapperFromText(String text, String mimeType) throws IOException {
		byte[] buf = text.getBytes("UTF-8");
		return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, mimeType);
	}

	public static InputStreamWrapper createInputStreamWrapperFromText(String text) throws IOException {
		return createInputStreamWrapperFromText(text,"text/xml; charset=utf-8");
	}

	@Override
	public int read() throws IOException {
		return stream.read();	
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return stream.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}
	
	@Override
	public int available() throws IOException {
		return stream.available();
	}
	
	@Override
	public void close() throws IOException {
		stream.close();
	}
	
	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		stream.mark(readlimit);
	}
	
	@Override
	public long skip(long n) throws IOException {
		return stream.skip(n);
	}
	
	@Override
	public synchronized void reset() throws IOException {
		stream.reset();
	}
	
}
