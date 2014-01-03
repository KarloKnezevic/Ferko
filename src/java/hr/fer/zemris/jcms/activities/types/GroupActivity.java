package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class GroupActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;

	private String groupName;
	private String parentGroupName;
	
	/**
	 * Kakva je ovo poruka:
	 * <ul>
	 * <li> 1 - dodijeljena Vam je grupa groupName</li>
	 * <li> 2 - uklonjeni ste iz grupe groupName</li>
	 * </ul>
	 */
	private int kind;
	
	/**
	 * Konstruktor. Predajemo nazive umjesto id-ova za slučaj kada se
	 * s korisnicima brisu i grupe (pa ne bi bilo moguce rekonstruirati naziv).
	 * 
	 * @param date datum
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param userID identifikator korisnika
	 * @param kind vrsta događaja
	 * @param groupName naziv grupe
	 * @param parentGroupName naziv grupe roditelja
	 */
	public GroupActivity(Date date, String courseInstanceID, Long userID,
			int kind, String groupName, String parentGroupName) {
		super(userID, date, courseInstanceID);
		this.kind = kind;
		this.groupName = groupName;
		this.parentGroupName = parentGroupName;
	}

	public int getKind() {
		return kind;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getParentGroupName() {
		return parentGroupName;
	}
}
