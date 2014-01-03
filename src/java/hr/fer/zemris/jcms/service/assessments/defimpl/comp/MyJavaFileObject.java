package hr.fer.zemris.jcms.service.assessments.defimpl.comp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

public class MyJavaFileObject implements JavaFileObject {

	private JavaFileObject delegate;
	
	public MyJavaFileObject(JavaFileObject delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Modifier getAccessLevel() {
		Modifier m = delegate.getAccessLevel();
		System.out.println("[MyJavaFileObject] getAccessLevel()="+m);
		return m;
	}

	@Override
	public Kind getKind() {
		Kind k = delegate.getKind();
		System.out.println("[MyJavaFileObject] getKind()="+k);
		return k;
	}

	@Override
	public NestingKind getNestingKind() {
		NestingKind k = delegate.getNestingKind();
		System.out.println("[MyJavaFileObject] getNestingKind()="+k);
		return k;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		boolean k = delegate.isNameCompatible(simpleName, kind);
		System.out.println("[MyJavaFileObject] isNameCompatible(simpleName="+simpleName+",kind="+kind+")="+k);
		return k;
	}

	@Override
	public boolean delete() {
		boolean k = delegate.delete();
		System.out.println("[MyJavaFileObject] delete()="+k);
		return k;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		CharSequence k = delegate.getCharContent(ignoreEncodingErrors);
		System.out.println("[MyJavaFileObject] getCharContent(ignoreEncodingErrors="+ignoreEncodingErrors+")="+k);
		return k;
	}

	@Override
	public long getLastModified() {
		long k = delegate.getLastModified();
		System.out.println("[MyJavaFileObject] getLastModified()="+k);
		return k;
	}

	@Override
	public String getName() {
		String k = delegate.getName();
		System.out.println("[MyJavaFileObject] getName()="+k);
		return k;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		InputStream k = delegate.openInputStream();
		System.out.println("[MyJavaFileObject] openInputStream()=?");
		return k;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		OutputStream k = delegate.openOutputStream();
		System.out.println("[MyJavaFileObject] openOutputStream()=?");
		return k;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		Reader k = delegate.openReader(ignoreEncodingErrors);
		System.out.println("[MyJavaFileObject] openReader(ignoreEncodingErrors="+ignoreEncodingErrors+")=?");
		return k;
	}

	@Override
	public Writer openWriter() throws IOException {
		Writer k = delegate.openWriter();
		System.out.println("[MyJavaFileObject] openWriter()=?");
		return k;
	}

	@Override
	public URI toUri() {
		URI k = delegate.toUri();
		System.out.println("[MyJavaFileObject] toURI()="+k);
		return k;
	}

}
