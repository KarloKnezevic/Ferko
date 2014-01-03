package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class ApplicationActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;

	/** StudentApplication_id **/
	private long studentApplicationId;
	
	/**
	 * Konstruktor.
	 * 
	 * @param date datum
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param userID identifikator korisnika
	 * @param studentApplicationId identifikator prijave
	 */
	public ApplicationActivity(Date date, String courseInstanceID, Long userID,
			long studentApplicationId) {
		super(userID, date, courseInstanceID);
		this.studentApplicationId = studentApplicationId;
	}
	
	public long getStudentApplicationId() {
		return studentApplicationId;
	}
}
