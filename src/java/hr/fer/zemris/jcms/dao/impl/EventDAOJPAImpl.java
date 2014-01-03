package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.EventDAO;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseWideEvent;
import hr.fer.zemris.jcms.model.GlobalEvent;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserSpecificEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public class EventDAOJPAImpl implements EventDAO {

	@Override
	public AbstractEvent get(EntityManager em, Long id) {
		return em.find(AbstractEvent.class, id);
	}

	@Override
	public GroupWideEvent getGroupWideEvent(EntityManager em, Long id) {
		return em.find(GroupWideEvent.class, id);
	}
	
	@Override
	public AbstractEvent listGroupEvents(EntityManager em, Group g) {
		// TODO: implementiraj!
		return null;
	}

	@Override
	public void remove(EntityManager em, AbstractEvent event) {
		em.remove(event);
	}

	@Override
	public void save(EntityManager em, AbstractEvent event) {
		em.persist(event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupWideEvent> listSemesterLectureEvents(EntityManager em, String semesterID) {
		return (List<GroupWideEvent>)em.createNamedQuery("GroupWideEvent.findAllSemLecture")
			.setParameter("specifier", semesterID+"/satnica/P")
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CourseWideEvent> listCourseWideEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		if(fromDate!=null && toDate!=null) {
			return uniqueCWE((List<CourseWideEvent>)em.createNamedQuery("CourseWideEvent.findForUser2")
				.setParameter("user", user)
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList());
		} else {
			return uniqueCWE((List<CourseWideEvent>)em.createNamedQuery("CourseWideEvent.findForUser").setParameter("user", user).getResultList());
		}
	}
	
	private List<CourseWideEvent> uniqueCWE(List<CourseWideEvent> resultList) {
		if(resultList==null || resultList.isEmpty()) return resultList;
		List<CourseWideEvent> list = new ArrayList<CourseWideEvent>(resultList.size());
		Set<Long> ids = new HashSet<Long>(resultList.size()*2);
		for(CourseWideEvent cwe : resultList) {
			if(ids.add(cwe.getId())) {
				list.add(cwe);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GlobalEvent> listGlobalEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		if(fromDate!=null && toDate!=null) {
			return (List<GlobalEvent>)em.createNamedQuery("GlobalEvent.findForUser2")
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList();
		} else {
			return (List<GlobalEvent>)em.createNamedQuery("GlobalEvent.findForUser").getResultList();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<GroupWideEvent> listGroupWideEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		if(fromDate!=null && toDate!=null) {
			return uniqueGWE((List<GroupWideEvent>)em.createNamedQuery("GroupWideEvent.findForUser2")
				.setParameter("user", user)
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList());
		} else {
			return uniqueGWE((List<GroupWideEvent>)em.createNamedQuery("GroupWideEvent.findForUser").setParameter("user", user).getResultList());
		}
	}
	
	private List<GroupWideEvent> uniqueGWE(List<GroupWideEvent> resultList) {
		if(resultList==null || resultList.isEmpty()) return resultList;
		List<GroupWideEvent> list = new ArrayList<GroupWideEvent>(resultList.size());
		Set<Long> ids = new HashSet<Long>(resultList.size()*2);
		for(GroupWideEvent gwe : resultList) {
			if(ids.add(gwe.getId())) {
				list.add(gwe);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserSpecificEvent> listUserSpecificEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		if(fromDate!=null && toDate!=null) {
			return (List<UserSpecificEvent>)em.createNamedQuery("UserSpecificEvent.findForUser2")
				.setParameter("user", user)
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList();
		} else {
			return (List<UserSpecificEvent>)em.createNamedQuery("UserSpecificEvent.findForUser").setParameter("user", user).getResultList();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractEvent> listEventsForCourseInstance(EntityManager em, CourseInstance courseInstance, Date fromDate, Date toDate) {
		List<AbstractEvent> events1;
		List<AbstractEvent> events2;
		if(fromDate!=null && toDate!=null) {
			events1 = (List<AbstractEvent>)em.createNamedQuery("CourseWideEvent.findForCourseInstance2")
				.setParameter("courseInstance", courseInstance)
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList();
		} else {
			events1 = (List<AbstractEvent>)em.createNamedQuery("CourseWideEvent.findForCourseInstance").setParameter("courseInstance", courseInstance).getResultList();
		}
		if(fromDate!=null && toDate!=null) {
			events2 = (List<AbstractEvent>)em.createNamedQuery("GroupWideEvent.findForCourseInstance2")
				.setParameter("courseInstanceID", courseInstance.getId())
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList();
		} else {
			events2 = (List<AbstractEvent>)em.createNamedQuery("GroupWideEvent.findForCourseInstance").setParameter("courseInstanceID", courseInstance.getId()).getResultList();
		}
		
		List<AbstractEvent> events = new ArrayList<AbstractEvent>(events1.size()+events2.size());
		events.addAll(events1);
		events.addAll(events2);
		return events;
	}
}
