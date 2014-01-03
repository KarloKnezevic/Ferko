package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.dao.ApplicationDAO;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;

import java.util.List;

import javax.persistence.EntityManager;

public class ApplicationDAOJPAImpl implements ApplicationDAO {

	@Override
	public StudentApplication get(EntityManager em, Long id) {
		return em.find(StudentApplication.class, id);
	}

	@Override
	public ApplicationDefinition getDefinition(EntityManager em, Long id) {
		return em.find(ApplicationDefinition.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StudentApplication> listStudentApplications(EntityManager em, String courseId) {
		return (List<StudentApplication>) em.createNamedQuery(
			"StudentApplication.list")
			.setParameter("courseID", courseId)
			.getResultList();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ApplicationDefinition> listDefinitions(EntityManager em,
			String courseId) {
		return (List<ApplicationDefinition>) em.createNamedQuery(
				"ApplicationDefinition.list")
				.setParameter("courseID", courseId)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StudentApplication> listForUser(EntityManager em,
			User user, String courseId) {
		return (List<StudentApplication>) em.createNamedQuery(
				"StudentApplication.listForUser")
				.setParameter("user", user)
				.setParameter("courseID", courseId)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public StudentApplication getApplicationForUser(EntityManager em, User user, Long defId) {
		List<StudentApplication> list = (List<StudentApplication>) em.createNamedQuery(
			"StudentApplication.getForUser")
			.setParameter("defID", defId)
			.setParameter("user", user)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
		}

	@SuppressWarnings("unchecked")
	@Override
	public List<StudentApplication> listForDefinition(EntityManager em,
			String courseId, Long defId) {
		return (List<StudentApplication>) em.createNamedQuery(
			"StudentApplication.listForDefinition")
			.setParameter("defID", defId)
			.setParameter("courseID", courseId)
			.getResultList();
	}
	@Override
	public void remove(EntityManager em, StudentApplication application) {
		em.remove(application);
	}

	@Override
	public void remove(EntityManager em, ApplicationDefinition definition) {
		em.remove(definition);
	}

	@Override
	public void save(EntityManager em, StudentApplication application) {
		em.persist(application);
	}

	@Override
	public void save(EntityManager em, ApplicationDefinition definition) {
		em.persist(definition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ApplicationDefinition getForShortName(EntityManager em,
			CourseInstance courseInstance, String shortName) {
		List<ApplicationDefinition> adefList = (List<ApplicationDefinition>)em.createNamedQuery(
		"ApplicationDefinition.listForCIAndSN")
		.setParameter("courseInstance", courseInstance)
		.setParameter("shortName", shortName)
		.getResultList();
		if(adefList==null || adefList.isEmpty()) return null;
		return adefList.get(0);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<StudentApplicationShortBean> listShortBeansFor(
			EntityManager em, CourseInstance courseInstance, String shortName) {
		return (List<StudentApplicationShortBean>) em.createNamedQuery(
		"StudentApplication.listForCourseInstanceAndSN")
		.setParameter("courseInstance", courseInstance)
		.setParameter("shortName", shortName)
		.getResultList();
	}
}
