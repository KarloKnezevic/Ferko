package hr.fer.zemris.jcms.dao.impl;

import java.util.Date
;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import hr.fer.zemris.jcms.dao.ActivityDAO;
import hr.fer.zemris.jcms.model.Activity;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;

public class ActivityDAOJPAImpl implements ActivityDAO {

	@Override
	public Activity get(EntityManager em, Long id) {
		return em.find(Activity.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> listLastNForUser(EntityManager em, Date since,
			User user, int n) {
		Query nq = em.createNamedQuery(since==null ? "Activity.forUser1" : "Activity.forUser2");
		nq.setParameter("user", user);
		if(since!=null) nq.setParameter("date", since);
		nq.setMaxResults(n);
		return (List<Activity>)nq.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> listLastNForUserAndCourse(EntityManager em,
			Date since, User user, CourseInstance ci, int n) {
		Query nq = em.createNamedQuery(since==null ? "Activity.forUser3" : "Activity.forUser4");
		nq.setParameter("user", user);
		if(since!=null) nq.setParameter("date", since);
		nq.setParameter("context", "cid="+ci.getId());
		nq.setMaxResults(n);
		return (List<Activity>)nq.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> listForUser(EntityManager em, Date since,
			User user) {
		Query nq = em.createNamedQuery(since==null ? "Activity.forUser5" : "Activity.forUser6");
		nq.setParameter("user", user);
		if(since!=null) nq.setParameter("date", since);
		return (List<Activity>)nq.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> listForUserAndCourse(EntityManager em,
			Date since, User user, CourseInstance ci) {
		Query nq = em.createNamedQuery(since==null ? "Activity.forUser7" : "Activity.forUser8");
		nq.setParameter("user", user);
		if(since!=null) nq.setParameter("date", since);
		nq.setParameter("context", "cid="+ci.getId());
		return (List<Activity>)nq.getResultList();
	}

	@Override
	public void remove(EntityManager em, Activity activity) {
		em.remove(activity);
	}

	@Override
	public void save(EntityManager em, Activity activity) {
		em.persist(activity);
	}

}
