package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.CourseInstanceKeyValueDAO;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;

import java.util.List;

import javax.persistence.EntityManager;

public class CourseInstanceKeyValueDAOJPAImpl implements CourseInstanceKeyValueDAO {

	@SuppressWarnings("unchecked")
	@Override
	public CourseInstanceKeyValue get(EntityManager em,
			CourseInstance courseInstance, String name) {
		List<CourseInstanceKeyValue> list = (List<CourseInstanceKeyValue>)em.createNamedQuery("CourseInstanceKeyValue.load").setParameter("key", name).setParameter("ci", courseInstance).getResultList();
		if(list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseInstanceKeyValue> list(EntityManager em,
			CourseInstance courseInstance) {
		List<CourseInstanceKeyValue> list = (List<CourseInstanceKeyValue>)em.createNamedQuery("CourseInstanceKeyValue.list").setParameter("ci", courseInstance).getResultList();
		return list;
	}

	@Override
	public void remove(EntityManager em, CourseInstanceKeyValue keyValue) {
		em.remove(keyValue);
	}
	
	@Override
	public void save(EntityManager em, CourseInstanceKeyValue keyValue) {
		em.persist(keyValue);
	}
	
}
