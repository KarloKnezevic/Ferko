package hr.fer.zemris.jcms.service.assessments.defimpl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;

public class DynaClassWeakReference extends WeakReference<Class<?>> {

	private int version;
	private Long id;
	private Map<Long,DynaClassWeakReference> map;
	
	public DynaClassWeakReference(Class<?> referent,
			ReferenceQueue<? super Class<?>> q, Long id, int version, Map<Long,DynaClassWeakReference> map) {
		super(referent, q);
		this.id = id;
		this.version = version;
		this.map = map;
	}

	public int getVersion() {
		return version;
	}
	
	public Long getId() {
		return id;
	}
	
	public Map<Long, DynaClassWeakReference> getMap() {
		return map;
	}
}
