package hr.fer.zemris.jcms.dao.impl;

import java.util.List;

import hr.fer.zemris.jcms.dao.CourseDAO;
import hr.fer.zemris.jcms.model.Course;

import javax.persistence.EntityManager;

public class CourseDAOJPAImpl implements CourseDAO {

	@Override
	public Course get(EntityManager em, String isvuCode) {
		return em.find(Course.class, isvuCode);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Course> getAllWithoutCategory(EntityManager em) {
		return em.createNamedQuery("Course.listAllWithoutCategory").getResultList();
	}

	@Override
	public void remove(EntityManager em, Course c) {
		em.remove(c);
	}

	@Override
	public void save(EntityManager em, Course c) {
		em.persist(c);
	}

}
