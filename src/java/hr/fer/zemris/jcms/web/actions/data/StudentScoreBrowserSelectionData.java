package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.StudentScoreBrowserSelection;

/**
 * Podatkovna struktura za akciju {@link StudentScoreBrowserSelection}.
 *  
 * @author marcupic
 *
 */
public class StudentScoreBrowserSelectionData extends BaseCourseInstance {

	private String courseInstanceID;
	private String kind;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public StudentScoreBrowserSelectionData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public String getKind() {
		return kind;
	}
    public void setKind(String kind) {
		this.kind = kind;
	}
}
