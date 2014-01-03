package hr.fer.zemris.jcms.dao;

import java.util.List;

import hr.fer.zemris.jcms.model.Course;

import javax.persistence.EntityManager;

public interface CourseDAO {

	public Course get(EntityManager em, String isvuCode);
	public List<Course> getAllWithoutCategory(EntityManager em);
	public void save(EntityManager em, Course c);
	public void remove(EntityManager em, Course c);
	
}
