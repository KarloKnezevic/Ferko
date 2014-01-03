package hr.fer.zemris.jcms.service.assessments.defimpl.comp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.apache.log4j.Logger;

public class MyJavaFileManager implements JavaFileManager {

	public static final Logger logger = Logger.getLogger(MyJavaFileManager.class.getCanonicalName());

	JavaFileManager delegate = null;
	Map<String,MemoryJavaFileObject> map = new HashMap<String, MemoryJavaFileObject>();
	String packagePrefix;
	
	public void releaseDelegateFileManager() {
		delegate = null;
	}
	
	public Map<String, MemoryJavaFileObject> getMap() {
		return map;
	}
	
	public MyJavaFileManager(JavaFileManager delegate, String packagePrefix) {
		super();
		this.delegate = delegate;
		this.packagePrefix = packagePrefix;
	}

	@Override
	public void close() throws IOException {
		//System.out.println("****[Delegate]: close();");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		delegate.close();
	}

	@Override
	public void flush() throws IOException {
		//System.out.println("****[Delegate]: flush();");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		delegate.flush();
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		//System.out.println("****[Delegate]: getClassLoader(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"});");
		if(delegate==null) return null;
		return delegate.getClassLoader(location);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		//System.out.println("****[Delegate]: getFileForInput(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"},package="+packageName+", relativaName="+relativeName+");");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		return delegate.getFileForInput(location, packageName, relativeName);
	}

	@Override
	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling) throws IOException {
		//System.out.println("****[Delegate]: getFileForOutput(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"},package="+packageName+", relativaName="+relativeName+", sibling="+sibling+");");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		return delegate.getFileForOutput(location, packageName, relativeName, sibling);
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		//System.out.println("****[Delegate]: getJavaFileForInput(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"},className="+className+", kind="+kind+");");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		return delegate.getJavaFileForInput(location, className, kind);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		//System.out.println("****[Delegate]: getJavaFileForOutput(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"},className="+className+", kind="+kind+", sibling="+sibling+");");
		if(className.startsWith(packagePrefix)) {
			MemoryJavaFileObject m = map.get(className);
			if(m==null) {
				m = new MemoryJavaFileObject(className);
				map.put(m.getName(), m);
			}
			return m;
		}
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		return delegate.getJavaFileForOutput(location, className, kind, sibling);
	}

	@Override
	public boolean handleOption(String current, Iterator<String> remaining) {
		//System.out.println("****[Delegate]: handleOption(current="+current+",...);");
		if(delegate==null) return false;
		return delegate.handleOption(current, remaining);
	}

	@Override
	public boolean hasLocation(Location location) {
		//System.out.println("****[Delegate]: hasLocation(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"});");
		if(delegate==null) return false;
		return delegate.hasLocation(location);
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		//System.out.println("****[Delegate]: inferBinaryName(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"}, file="+file+");");
		if(delegate==null) return null;
		return delegate.inferBinaryName(location, file);
	}

	@Override
	public boolean isSameFile(FileObject a, FileObject b) {
		//System.out.println("****[Delegate]: isSameFile(a="+a+", b="+b+");");
		if(delegate==null) throw new IllegalArgumentException("Delegate File Manager was closed!");
		return delegate.isSameFile(a, b);
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		//System.out.println("****[Delegate]: list(location={name="+location.getName()+", isOutputLocation="+location.isOutputLocation()+"},package="+packageName+", kinds="+kinds+", recurse="+recurse+");");
		if(delegate==null) throw new IOException("Delegate File Manager was closed!");
		return delegate.list(location, packageName, kinds, recurse);
	}

	@Override
	public int isSupportedOption(String option) {
		//System.out.println("****[Delegate]: isSupportedOption(option="+option+");");
		if(delegate==null) return -1;
		return delegate.isSupportedOption(option);
	}
}
