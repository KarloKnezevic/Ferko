package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.CourseInstance;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class BaseCourseInstance extends AbstractActionData implements HasCourseInstance {
	
	protected CourseInstance courseInstance;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public BaseCourseInstance(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	/**
	 * Podaci o primjerku kolegija.
	 * @return primjerak kolegija
	 */
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
}
