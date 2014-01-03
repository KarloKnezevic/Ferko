package hr.fer.zemris.jcms.service.assessments.defimpl.comp;

import java.util.HashMap;
import java.util.Map;

public class MyDynamicClassLoader extends ClassLoader {
	
	Map<String,MemoryJavaFileObject> map;
	Map<String,Class<?>> cache = new HashMap<String, Class<?>>();
	
	public MyDynamicClassLoader(Map<String, MemoryJavaFileObject> map, ClassLoader parent) {
		super(parent);
		this.map = map;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> c = cache.get(name);
		if(c!=null) {
			return c;
		}
		MemoryJavaFileObject m = map.get(name);
		if(m!=null && m.getData()!=null) {
			byte[] d = m.getData();
			c = this.defineClass(name, d, 0, d.length);
			this.resolveClass(c);
			cache.put(name, c);
			return c;
		}
		return super.findClass(name);
	}
}
