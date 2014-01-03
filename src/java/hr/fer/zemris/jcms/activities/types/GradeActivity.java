package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class GradeActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;

	/** Ocjena. **/
	private byte grade;
	
	/** Rang. **/
	private int rang;
	
	/** Vrsta aktivnosti. **/
	private ActivityEventKind kind;
	
	/**
	 * Konstruktor.
	 * 
	 * @param date datum
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param userID identifikator korisnika
	 * @param grade ocjena
	 * @param rang rang
	 * @param kind vrsta događaja
	 */
	public GradeActivity(Date date, String courseInstanceID, Long userID,
			byte grade, int rang, ActivityEventKind kind) {
		super(userID, date, courseInstanceID);
		this.grade = grade;
		this.rang = rang;
		this.kind = kind;
	}
	
	/**
	 * Dohvaća ocjenu.
	 * 
	 * @return ocjena
	 */
	public byte getGrade() {
		return grade;
	}
	
	/**
	 * Dohvaća rang.
	 * 
	 * @return rang
	 */
	public int getRang() {
		return rang;
	}

	/**
	 * Dohvaća vrstu aktivnosti (definirana ocjena, obrisana ocjena, ...)
	 * 
	 * @return vrsta događaja
	 */
	public ActivityEventKind getKind() {
		return kind;
	}
}
