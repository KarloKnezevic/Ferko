package hr.fer.zemris.jcms.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class PersistenceUtil {

	private static ThreadLocal<Data> threadLocal = new ThreadLocal<Data>();
	private static EntityManagerFactory emf;
	
	public static <T> T executeSingleDatabaseOperation(DatabaseOperation<T> dbo) {
		T result = null;
		EntityManager em = PersistenceUtil.getEntityManager();
		try {
			PersistenceUtil.beginTransaction();
			result = dbo.executeOperation(em);
			PersistenceUtil.commitTransaction();
		} catch(RuntimeException ex) {
			PersistenceUtil.rollbackIfNeeded();
			throw ex;
		} finally {
			PersistenceUtil.closeEntityManager();
		}
		return result;
	}

	public static <T> T executeSingleDatabaseOperation(DatabaseOperationExt<T> dbo) {
		T result = null;
		EntityManager em = PersistenceUtil.getEntityManager();
		try {
			PersistenceUtil.beginTransaction();
			result = dbo.executeOperationExt(em, PersistenceUtil.getEntityTransaction());
			PersistenceUtil.commitTransaction();
		} catch(RuntimeException ex) {
			PersistenceUtil.rollbackIfNeeded();
			throw ex;
		} finally {
			PersistenceUtil.closeEntityManager();
		}
		return result;
	}

	public static void initSingleTon(EntityManagerFactory emf) {
		PersistenceUtil.emf = emf;
	}
	
	public static void clearSingleTon() {
		emf = null;
	}
	
	public static EntityManager getEntityManager() {
		Data d = threadLocal.get();
		if(d != null) {
			return d.em;
		}
		if(emf == null) throw new IllegalStateException("Persistence provider is not available.");
		d = new Data();
		d.em = emf.createEntityManager();
		try {
			d.tx = d.em.getTransaction();
		} catch(RuntimeException ex) {
			d.em.close();
			throw ex;
		}
		threadLocal.set(d);
		return d.em;
	}

	public static void closeEntityManager() {
		Data d = threadLocal.get();
		if(d == null) {
			return;
		}
		d.em.close();
		threadLocal.remove();
	}

	private static EntityTransaction getEntityTransaction() {
		Data d = threadLocal.get();
		if(d == null) {
			return null;
		}
		return d.tx;
	}
	
	public static void rollbackIfNeeded() {
		Data d = threadLocal.get();
		if(d == null) {
			return;
		}
		if(d.tx.isActive()) d.tx.rollback();
	}
	
	public static boolean isTransactionActive() {
		Data d = threadLocal.get();
		if(d == null) {
			throw new IllegalStateException("Entity manager is not accessible. Are you calling PersistenceUtil.isTransactionActive() before PersistenceUtil.getEntityManager()?");
		}
		return d.tx.isActive();
	}
	
	public static void beginTransaction() {
		Data d = threadLocal.get();
		if(d == null) {
			throw new IllegalStateException("Entity manager is not accessible. Are you calling PersistenceUtil.beginTransaction() before PersistenceUtil.getEntityManager()?");
		}
		d.tx.begin();
	}
	
	public static void commitTransaction() {
		Data d = threadLocal.get();
		if(d == null) {
			throw new IllegalStateException("Entity manager is not accessible. Are you calling PersistenceUtil.commitTransaction() before PersistenceUtil.getEntityManager()?");
		}
		d.tx.commit();
	}
	
	public static void rollbackTransaction() {
		Data d = threadLocal.get();
		if(d == null) {
			throw new IllegalStateException("Entity manager is not accessible. Are you calling PersistenceUtil.rollbackTransaction() before PersistenceUtil.getEntityManager()?");
		}
		d.tx.rollback();
	}
	
	private static class Data {
		EntityManager em;
		EntityTransaction tx;
	}
}
