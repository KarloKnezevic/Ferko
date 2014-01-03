package hr.fer.zemris.jcms.web.support;

import javax.persistence.EntityManager;

public class TransactionalMethodSupport {

	private static ThreadLocal<EntityManager> vars = new ThreadLocal<EntityManager>();

	public static EntityManager getEntityManager() {
		return vars.get();
	}
	
	public static void set(EntityManager em) {
		if(vars.get()!=null) {
			throw new RuntimeException("Double-set of EntityManager attempted.");
		}
		vars.set(em);
	}
	
	public static void clear() {
		vars.remove();
	}
	
}
