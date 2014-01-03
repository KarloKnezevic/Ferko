package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.KeyValue;

import java.util.List;

import javax.persistence.EntityManager;

public interface KeyValueDAO {

	public KeyValue get(EntityManager em, String name);
	public void save(EntityManager em, KeyValue keyValue);
	public void update(EntityManager em, KeyValue keyValue);
	public void remove(EntityManager em, KeyValue keyValue);
	public List<KeyValue> list(EntityManager em);
}
