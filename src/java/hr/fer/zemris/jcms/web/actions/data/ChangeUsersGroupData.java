package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class ChangeUsersGroupData extends BaseGroup {

	private Group marketPlaceGroup;
	private UserGroup userGroup;
	private List<Group> offeredGroups;
	private Long groupID;
	private Long ugID;
	private Long mpID;
	private Long toGroupID;
	private Long viewedGroupID;
	
	public ChangeUsersGroupData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public UserGroup getUserGroup() {
		return userGroup;
	}
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	public List<Group> getOfferedGroups() {
		return offeredGroups;
	}
	public void setOfferedGroups(List<Group> offeredGroups) {
		this.offeredGroups = offeredGroups;
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public Long getUgID() {
		return ugID;
	}
	public void setUgID(Long ugID) {
		this.ugID = ugID;
	}

	public Long getMpID() {
		return mpID;
	}
	public void setMpID(Long mpID) {
		this.mpID = mpID;
	}
	
	public Group getMarketPlaceGroup() {
		return marketPlaceGroup;
	}
	public void setMarketPlaceGroup(Group marketPlaceGroup) {
		this.marketPlaceGroup = marketPlaceGroup;
	}
	
	public Long getToGroupID() {
		return toGroupID;
	}
	public void setToGroupID(Long toGroupID) {
		this.toGroupID = toGroupID;
	}
	
	public Long getViewedGroupID() {
		return viewedGroupID;
	}
	public void setViewedGroupID(Long viewedGroupID) {
		this.viewedGroupID = viewedGroupID;
	}
}
