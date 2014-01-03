package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.CourseInstance;

import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;

import java.util.List;

import javax.persistence.EntityManager;

public interface CourseInstanceKeyValueDAO {
	public CourseInstanceKeyValue get(EntityManager em, CourseInstance courseInstance, String name);
	public void save(EntityManager em, CourseInstanceKeyValue keyValue);
	public void remove(EntityManager em, CourseInstanceKeyValue keyValue);
	public List<CourseInstanceKeyValue> list(EntityManager em, CourseInstance courseInstance);
}
