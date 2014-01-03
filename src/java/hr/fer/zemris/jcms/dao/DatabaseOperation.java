package hr.fer.zemris.jcms.dao;

import javax.persistence.EntityManager;

public interface DatabaseOperation<T> {
	public T executeOperation(EntityManager em);
}
