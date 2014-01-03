package hr.fer.zemris.jcms.activities.types;

import java.util.Date;

public class ComponentGroupActivity extends GroupActivity {

	private static final long serialVersionUID = 1L;

	private String groupRoot;
	private int itemPosition;
	
	/**
	 * Konstruktor.
	 * 
	 * @param date datum
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param userID identifikator korisnika
	 * @param kind vrsta događaja
	 * @param groupName naziv grupe
	 * @param parentGroupName naziv grupe roditelja
	 */
	public ComponentGroupActivity(Date date, String courseInstanceID, Long userID,
			int kind, String groupName, String parentGroupName, String groupRoot, int itemPosition) {
		super(date, courseInstanceID, userID, kind, groupName, parentGroupName);
		this.groupRoot = groupRoot;
		this.itemPosition = itemPosition;
	}

	public String getGroupRoot() {
		return groupRoot;
	}
	
	public int getItemPosition() {
		return itemPosition;
	}
}
