package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseWideEvent;
import hr.fer.zemris.jcms.model.GlobalEvent;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserSpecificEvent;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

public interface EventDAO {

	public AbstractEvent listGroupEvents(EntityManager em, Group g);
	public GroupWideEvent getGroupWideEvent(EntityManager em, Long id);
	public AbstractEvent get(EntityManager em, Long id);
	public void save(EntityManager em, AbstractEvent event);
	public void remove(EntityManager em, AbstractEvent event);
	public List<GroupWideEvent> listSemesterLectureEvents(EntityManager em, String semesterID);
	public List<UserSpecificEvent> listUserSpecificEvents(EntityManager em, User user, Date fromDate, Date toDate);
	public List<GroupWideEvent> listGroupWideEvents(EntityManager em, User user, Date fromDate, Date toDate);
	public List<CourseWideEvent> listCourseWideEvents(EntityManager em, User user, Date fromDate, Date toDate);
	public List<GlobalEvent> listGlobalEvents(EntityManager em, User user, Date fromDate, Date toDate);
	public List<AbstractEvent> listEventsForCourseInstance(EntityManager em, CourseInstance courseInstance, Date fromDate, Date toDate);
}
