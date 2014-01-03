package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class GroupEditData extends BaseGroup {

	private String confirmed;
	private Long groupID;
	private String groupName;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public GroupEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(String confirmed) {
		this.confirmed = confirmed;
	}
}
