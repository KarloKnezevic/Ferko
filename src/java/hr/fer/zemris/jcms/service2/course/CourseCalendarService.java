package hr.fer.zemris.jcms.service2.course;

import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.EventsService;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseEventsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

public class CourseCalendarService {

	public static void getCourseSimpleCalendar(EntityManager em, ShowCourseEventsData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setEvents(EventsService.listForCourseInstance(em, data.getCourseInstance(), data.getDateFrom(), data.getDateTo()));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
}
