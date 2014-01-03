package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.AssessmentFlagValueBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentFlagDataData extends BaseAssessmentFlag {

	private String letter;
	private List<AssessmentFlagValueBean> flagValues;
	private String assessmentFlagID;
	private List<String> letters;
	private boolean confirmed;
	private String courseInstanceID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentFlagDataData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getLetter() {
		return letter;
	}
	public void setLetter(String letter) {
		this.letter = letter;
	}
	
	public List<String> getLetters() {
		return letters;
	}
	public void setLetters(List<String> letters) {
		this.letters = letters;
	}
	
	public List<AssessmentFlagValueBean> getFlagValues() {
		return flagValues;
	}
	public void setFlagValues(List<AssessmentFlagValueBean> flagValues) {
		this.flagValues = flagValues;
	}
	
	public String getAssessmentFlagID() {
		return assessmentFlagID;
	}
	public void setAssessmentFlagID(String assessmentFlagID) {
		this.assessmentFlagID = assessmentFlagID;
	}
	
	public boolean isConfirmed() {
		return confirmed;
	}
	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
