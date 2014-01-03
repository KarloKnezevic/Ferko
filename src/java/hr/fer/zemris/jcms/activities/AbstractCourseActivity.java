package hr.fer.zemris.jcms.activities;

import java.util.Date;

/**
 * Osnova za prijavu aktivnosti koje su vezane uz kolegij.
 * 
 * @author marcupic
 */
public class AbstractCourseActivity extends AbstractActivity {

	private static final long serialVersionUID = 1L;
	
	/** Identifikator primjerka kolegija **/
	private String courseInstanceID;
	
	/**
	 * Konstruktor.
	 * @param userID identifikator korisnika
	 * @param date datum aktivnosti
	 * @param courseInstanceID identifikator kolegija
	 */
	public AbstractCourseActivity(Long userID, Date date, String courseInstanceID) {
		super(userID, date);
		this.courseInstanceID = courseInstanceID;
	}
	
	/**
	 * Dohvat identifikatora primjerka kolegija.
	 * 
	 * @return identifikator primjerka kolegija
	 */
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
}
