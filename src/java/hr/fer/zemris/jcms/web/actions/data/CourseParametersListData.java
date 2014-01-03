package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.service2.course.parameters.CourseParametersService.ParameterAttributes;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.parameters.CourseParametersList;

/**
 * Podatkovna struktura za akciju {@link CourseParametersList}.
 *  
 * @author marcupic
 *
 */
public class CourseParametersListData extends BaseCourseInstance {
	
	private String courseInstanceID;
	private ParameterAttributes miSched;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseParametersListData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public ParameterAttributes getMiSched() {
		return miSched;
	}

    public void setMiSched(ParameterAttributes miSched) {
		this.miSched = miSched;
	}
}
