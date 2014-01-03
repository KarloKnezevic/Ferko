package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.InputStreamWrapper;

/**
 * Podatkovna struktura za akciju {@link ShowCourse}.
 *  
 * @author marcupic
 *
 */
public class CourseInstanceImageData extends BaseCourseInstance {

	private String courseInstanceID;
	private InputStreamWrapper stream;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseInstanceImageData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public InputStreamWrapper getStream() {
		return stream;
	}
	public void setStream(InputStreamWrapper stream) {
		this.stream = stream;
	}
	
}
