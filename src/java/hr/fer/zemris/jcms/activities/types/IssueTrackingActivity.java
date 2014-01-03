package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class IssueTrackingActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;

	/**
	 * Kakva je ovo poruka:
	 * <ul>
	 * <li> 1 - Novo pitanje/problem</li>
	 * <li> 2 - Odgovor asistenta</li>
	 * <li> 3 - Odgovor studenta</li>
	 * </ul>
	 */
	private int kind;
	
	/**
	 * O kojem se korisniku radi? (kontekst je jasan iz tipa poruke)
	 */
	private String username;
	
	/**
	 * ID pitanja/problema
	 */
	private Long issueID;
	
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
	public IssueTrackingActivity(Date date, String courseInstanceID, Long userID,
			int kind, String username, Long issueID) {
		super(userID, date, courseInstanceID);
		this.kind = kind;
		this.username = username;
		this.issueID = issueID;
	}

	public int getKind() {
		return kind;
	}
	
	public String getUsername() {
		return username;
	}

	public Long getIssueID() {
		return issueID;
	}
	
}
