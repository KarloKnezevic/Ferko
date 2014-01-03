package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.AdminAssessmentAppeal;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentAppeal}.
 *  
 * @author marcupic
 *
 */
public class AdminProcessAssessmentAppealData extends AbstractActionData implements HasCourseInstance {
	private CourseInstance courseInstance;
	private Assessment assessment;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminProcessAssessmentAppealData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	@Override
	public CourseInstance getCourseInstance() {
		return this.courseInstance;
	}

	@Override
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
}
