package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.parameters.CourseParameters2;

/**
 * Podatkovna struktura za akciju {@link CourseParameters2}.
 *  
 * @author marcupic
 *
 */
public class CourseParameters2Data extends BaseCourseInstance {
	
	private String courseInstanceID;
	private boolean wikiEnabled;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseParameters2Data(IMessageLogger messageLogger) {
		super(messageLogger);
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public boolean isWikiEnabled() {
		return wikiEnabled;
	}
    public void setWikiEnabled(boolean wikiEnabled) {
		this.wikiEnabled = wikiEnabled;
	}
    
}
