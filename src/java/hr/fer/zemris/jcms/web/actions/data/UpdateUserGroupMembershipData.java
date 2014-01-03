package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class UpdateUserGroupMembershipData extends BaseGroup {
	
	private Long groupID;
	private String text;
	private boolean removeOther;
	private Long mpID;
	
	public UpdateUserGroupMembershipData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public boolean isRemoveOther() {
		return removeOther;
	}
	public void setRemoveOther(boolean removeOther) {
		this.removeOther = removeOther;
	}

	public Long getMpID() {
		return mpID;
	}
	public void setMpID(Long mpID) {
		this.mpID = mpID;
	}

	
}
