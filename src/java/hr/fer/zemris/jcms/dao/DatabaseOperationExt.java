package hr.fer.zemris.jcms.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public interface DatabaseOperationExt<T> {
	public T executeOperationExt(EntityManager em, EntityTransaction tx);
}
