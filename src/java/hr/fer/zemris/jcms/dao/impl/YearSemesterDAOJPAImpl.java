package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.YearSemesterDAO;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;

import java.util.List;

import javax.persistence.EntityManager;

public class YearSemesterDAOJPAImpl implements YearSemesterDAO {

	@Override
	public YearSemester get(EntityManager em, String id) {
		return em.find(YearSemester.class, id);
	}

	@Override
	public void remove(EntityManager em, YearSemester y) {
		em.remove(y);
	}

	@Override
	public void save(EntityManager em, YearSemester y) {
		em.persist(y);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findUsersInSemester(EntityManager em, YearSemester y) {
		return (List<User>)em.createNamedQuery("Group.findAllSemUsers")
			.setParameter("compositeCourseID", y.getId()+"/%")
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<YearSemester> list(EntityManager em) {
		return (List<YearSemester>)em.createNamedQuery("YearSemester.list")
		.getResultList();
	}

}
