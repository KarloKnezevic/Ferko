package hr.fer.zemris.jcms.service.assessments.defimpl.comp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

public class MemoryJavaFileObject implements JavaFileObject {

	private ByteArrayOutputStream bos = null;
	private byte[] data = null;
	private String name;
	
	public MemoryJavaFileObject(String name) {
		super();
		this.name = name;
	}

	@Override
	public Modifier getAccessLevel() {
		return null;
	}

	@Override
	public Kind getKind() {
		return null;
	}

	@Override
	public NestingKind getNestingKind() {
		return null;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return false;
	}

	@Override
	public boolean delete() {
		return false;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		return null;
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return null;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		bos = new ByteArrayOutputStream();
		return bos;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return null;
	}

	@Override
	public Writer openWriter() throws IOException {
		return null;
	}

	@Override
	public URI toUri() {
		return null;
	}

	public byte[] getData() {
		if(data==null && bos!=null) {
			data = bos.toByteArray();
			bos = null;
		}
		return data;
	}
}
