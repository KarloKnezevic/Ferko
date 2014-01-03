package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class TransferUsersFromGroupData extends BaseGroup {

	private Long groupID;
	private Long sourceGroupID;
	private Long mpID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public TransferUsersFromGroupData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public Long getSourceGroupID() {
		return sourceGroupID;
	}
	public void setSourceGroupID(Long sourceGroupID) {
		this.sourceGroupID = sourceGroupID;
	}

	public Long getMpID() {
		return mpID;
	}
	public void setMpID(Long mpID) {
		this.mpID = mpID;
	}
}
