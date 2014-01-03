package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public class EventsService {

	public static List<AbstractEvent> listForUser(final String username, final Date fromDate, final Date toDate) {
		return PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<List<AbstractEvent>>() {
			@Override
			public List<AbstractEvent> executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserByUsername(em, username);
				return listForUser(em, user, fromDate, toDate);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static List<AbstractEvent> listForUser(EntityManager em, User user, Date fromDate, Date toDate) {
		if(user==null) {
			return new ArrayList<AbstractEvent>();
		}
		List<? extends AbstractEvent> glEvents = getGlobalEvents(em, user, fromDate, toDate);
		List<? extends AbstractEvent> cwEvents = getCourseWideEvents(em, user, fromDate, toDate);
		List<? extends AbstractEvent> gwEvents = getGroupWideEvents(em, user, fromDate, toDate);
		List<? extends AbstractEvent> usEvents = getUserSpecificEvents(em, user, fromDate, toDate);
		List<AbstractEvent> allEvents = new ArrayList<AbstractEvent>(
			glEvents.size() + cwEvents.size() + gwEvents.size() + usEvents.size()
		);
		
		Set<String> blockedEvents = new HashSet<String>(glEvents.size() + cwEvents.size() + gwEvents.size() + usEvents.size());
		for(AbstractEvent ev : gwEvents) {
			String ctx = ev.getContext();
			if(ctx!=null && ctx.length()!=0) {
				blockedEvents.add(ctx);
			}
		}
		Iterator<? extends AbstractEvent> it = cwEvents.iterator();
		while(it.hasNext()) {
			AbstractEvent ev = it.next();
			String ctx = ev.getContext();
			if(ctx!=null && ctx.length()!=0 && blockedEvents.contains(ctx)) {
				it.remove();
			}
		}
		
		((List)allEvents).addAll(glEvents);  glEvents = null;
		((List)allEvents).addAll(cwEvents);  cwEvents = null;
		((List)allEvents).addAll(gwEvents);  gwEvents = null;
		((List)allEvents).addAll(usEvents);  usEvents = null;
		Collections.sort(allEvents, EVENT_COMPARATOR);
		return allEvents;
	}

	protected static List<? extends AbstractEvent> getGlobalEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		return dh.getEventDAO().listGlobalEvents(em, user, fromDate, toDate);
	}

	protected static List<? extends AbstractEvent> getCourseWideEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		return dh.getEventDAO().listCourseWideEvents(em, user, fromDate, toDate);
	}

	protected static List<? extends AbstractEvent> getGroupWideEvents(EntityManager em,
			User user, Date fromDate, Date toDate) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		return dh.getEventDAO().listGroupWideEvents(em, user, fromDate, toDate);
	}

	protected static List<? extends AbstractEvent> getUserSpecificEvents(
			EntityManager em, User user, Date fromDate, Date toDate) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		return dh.getEventDAO().listUserSpecificEvents(em, user, fromDate, toDate);
	}
	
	/**
	 * Komparator evenata po datumima, i potom po dvoranama.
	 */
	public static final Comparator<AbstractEvent> EVENT_COMPARATOR = new Comparator<AbstractEvent>() {
		@Override
		public int compare(AbstractEvent o1, AbstractEvent o2) {
			int r = o1.getStart().compareTo(o2.getStart());
			if(r!=0) return r;
			r = o1.getId().compareTo(o2.getId());
			return r;
		}
	};

	public static List<AbstractEvent> listForCourseInstance(EntityManager em, CourseInstance courseInstance, Date dateFrom, Date dateTo) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<AbstractEvent> allEvents = dh.getEventDAO().listEventsForCourseInstance(em, courseInstance, dateFrom, dateTo);
		Collections.sort(allEvents, EVENT_COMPARATOR);
		return allEvents;
	}
}
