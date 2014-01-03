package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.UGPBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class ShowGroupUsersData extends BaseGroup {

	private Long groupID;
	private String relativePath;
	private String courseInstanceID;
	
	private List<UGPBean> allUsers;
	private List<Group> allGroups;
	private List<Group> managedGroups;
	private List<Group> transferGroups;
	private Group marketPlaceGroup;
	private GroupSupportedPermission gperm;
	private boolean transferEnabled;
	
	public boolean getTransferEnabled() {
		return transferEnabled;
	}
	public void setTransferEnabled(boolean transferEnabled) {
		this.transferEnabled = transferEnabled;
	}

	public List<Group> getTransferGroups() {
		return transferGroups;
	}
	public void setTransferGroups(List<Group> transferGroups) {
		this.transferGroups = transferGroups;
	}
	
	public GroupSupportedPermission getGperm() {
		return gperm;
	}
	public void setGperm(GroupSupportedPermission gperm) {
		this.gperm = gperm;
	}
	
	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
	
	public ShowGroupUsersData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<Group> getAllGroups() {
		return allGroups;
	}
	public void setAllGroups(List<Group> allGroups) {
		this.allGroups = allGroups;
	}
	
	public List<UGPBean> getAllUsers() {
		return allUsers;
	}
	public void setAllUsers(List<UGPBean> allUsers) {
		this.allUsers = allUsers;
	}

	public List<Group> getManagedGroups() {
		return managedGroups;
	}
	public void setManagedGroups(List<Group> managedGroups) {
		this.managedGroups = managedGroups;
	}
	public Group getMarketPlaceGroup() {
		return marketPlaceGroup;
	}
	public void setMarketPlaceGroup(Group marketPlaceGroup) {
		this.marketPlaceGroup = marketPlaceGroup;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
