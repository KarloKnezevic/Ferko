package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.assessments.AdminAssessmentFlagImport;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentFlagImport}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentFlagImportData extends BaseAssessmentFlag {
	
	private String text = null;
	private Long id;
	private String courseInstanceID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentFlagImportData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
