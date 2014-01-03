package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class MarketActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;

	/**
	 * Kakva je ovo poruka:
	 * <ul>
	 * <li> 1 - dobili ste direktnu ponudu iz grupe od korisnika</li>
	 * <li> 2 - grupa Vam je zamijenjena</li>
	 * </ul>
	 */
	private int kind;
	
	/**
	 * O kojoj se grupi radi? (kontekst je jasan iz tipa poruke)
	 * Ovo je naziv dotične grupe.
	 */
	private String groupName;
	
	/**
	 * O kojem se korisniku radi? (kontekst je jasan iz tipa poruke)
	 */
	private String username;
	
	/**
	 * ID grupe koja je burza.
	 */
	private Long parentGroupID;
	
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
	public MarketActivity(Date date, String courseInstanceID, Long userID,
			int kind, String groupName, String username, Long parentGroupID) {
		super(userID, date, courseInstanceID);
		this.kind = kind;
		this.groupName = groupName;
		this.username = username;
		this.parentGroupID = parentGroupID;
	}

	public Long getParentGroupID() {
		return parentGroupID;
	}
	
	public int getKind() {
		return kind;
	}

	public String getGroupName() {
		return groupName;
	}
	
	public String getUsername() {
		return username;
	}
}
