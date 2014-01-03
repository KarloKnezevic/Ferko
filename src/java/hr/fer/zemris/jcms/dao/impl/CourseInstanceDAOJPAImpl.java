package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.CourseInstanceDAO;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceIsvuData;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.CourseInstanceWithGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class CourseInstanceDAOJPAImpl implements CourseInstanceDAO {

	@Override
	public void save(EntityManager em, CourseInstanceIsvuData isvuData) {
		em.persist(isvuData);
	}
	
	@Override
	public CourseInstance get(EntityManager em, String id) {
		return em.find(CourseInstance.class, id);
	}

	@Override
	public void remove(EntityManager em, CourseInstance ci) {
		em.remove(ci);
	}

	@Override
	public void save(EntityManager em, CourseInstance ci) {
		em.persist(ci);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseInstance> findForSemester(EntityManager em,
			String yearSemesterID) {
		return (List<CourseInstance>)em.createNamedQuery("CourseInstance.listForYearSemesterKey").setParameter("yearSemesterID", yearSemesterID).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CourseInstanceWithGroup> findForUserAndSemester(EntityManager em, String yearSemesterID, User user) {
		List<Group> lectureGroups = (List<Group>)em.createNamedQuery("Group.findForUser")
			.setParameter("compositeCourseID", yearSemesterID+"/%")
			.setParameter("user", user)
			.getResultList();
		StringBuilder sb = new StringBuilder(1000);
		if(lectureGroups.isEmpty()) return new ArrayList<CourseInstanceWithGroup>();
		sb.append("SELECT ci FROM CourseInstance as ci WHERE ci.id IN ('").append(lectureGroups.get(0).getCompositeCourseID()).append('\'');
		for(int i = 1; i < lectureGroups.size(); i++) {
			sb.append(", \'").append(lectureGroups.get(i).getCompositeCourseID()).append('\'');
		}
		sb.append(")");
		List<CourseInstance> courseInstances = (List<CourseInstance>)em.createQuery(sb.toString()).getResultList();
		Map<String, CourseInstanceWithGroup> m = new HashMap<String, CourseInstanceWithGroup>();
		for(CourseInstance ci : courseInstances) {
			CourseInstanceWithGroup ciwg = new CourseInstanceWithGroup();
			ciwg.setCourseInstance(ci);
			m.put(ci.getId(), ciwg);
		}
		for(Group g : lectureGroups) {
			CourseInstanceWithGroup ciwg = m.get(g.getCompositeCourseID());
			ciwg.setGroup(g);
		}
		List<CourseInstanceWithGroup> result = new ArrayList<CourseInstanceWithGroup>();
		result.addAll(m.values());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isCourseStaffMember(EntityManager em,
			CourseInstance courseInstance, User user) {
		List<UserGroup> list = (List<UserGroup>)em.createNamedQuery("Group.isUserOnCourseStaff")
			.setParameter("compositeCourseID", courseInstance.getId())
			.setParameter("user", user)
			.getResultList();
		if(list==null || list.isEmpty()) return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CourseInstance> findForCourseStaff(EntityManager em,
			YearSemester yearSemester, User user) {
		return (List<CourseInstance>)em.createNamedQuery("CourseInstance.listForYearSemesterStaff")
			.setParameter("yearSemester", yearSemester)
			.setParameter("user", user)
			.setParameter("compositeCourseID", yearSemester.getId()+"/%")
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> findCourseUsers(EntityManager em, String courseCompositeID) {
		return (List<User>)em.createNamedQuery("Group.findCourseUsers")
			.setParameter("compositeCourseID", courseCompositeID)
			.getResultList();
	}
	
	@Override
	public CourseInstance findLastForCourse(EntityManager em, Course course) {
		try {
			return (CourseInstance)em.createNamedQuery("CourseInstance.listForCourse")
			.setParameter("courseIsvuCode", course.getIsvuCode())
			.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Grade> listGradesFor(EntityManager em,
			CourseInstance courseInstance) {
		return (List<Grade>)em.createNamedQuery("Grade.forCourseInstance")
		.setParameter("ci", courseInstance)
		.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Grade findGradeForCIAndUser(EntityManager em,
			CourseInstance courseInstance, User user) {
		List<Grade> list = (List<Grade>)em.createNamedQuery("Grade.forCourseInstanceAndUser")
		.setParameter("ci", courseInstance)
		.setParameter("user", user)
		.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@Override
	public void remove(EntityManager em, Grade g) {
		em.remove(g);
	}
	
	@Override
	public void save(EntityManager em, Grade g) {
		em.persist(g);
	}
	
}
