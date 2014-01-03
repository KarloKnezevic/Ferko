package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.KeyValueDAO;
import hr.fer.zemris.jcms.model.KeyValue;

import java.util.List;

import javax.persistence.EntityManager;

public class KeyValueDAOJPAImpl implements KeyValueDAO {

	@Override
	public KeyValue get(EntityManager em, String name) {
		return em.find(KeyValue.class, name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KeyValue> list(EntityManager em) {
		return (List<KeyValue>)em.createNamedQuery("KeyValue.list").getResultList();
	}

	@Override
	public void remove(EntityManager em, KeyValue keyValue) {
		em.remove(keyValue);
	}

	@Override
	public void save(EntityManager em, KeyValue keyValue) {
		em.persist(keyValue);
	}

	@Override
	public void update(EntityManager em, KeyValue keyValue) {
		em.merge(keyValue);
	}
}
