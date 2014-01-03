package hr.fer.zemris.jcms.web.actions.data;

import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ext.GroupOwnerFlat;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class SubgroupOwnersData extends BaseGroup {

	private Long groupID;
	private GroupOwnerFlat goBean = new GroupOwnerFlat();
	private Long groupOwnerID;
	private List<GroupBean> groupList = Collections.emptyList();
	private List<User> userList = Collections.emptyList();
	private GroupOwner owner;

	public SubgroupOwnersData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public GroupOwnerFlat getGoBean() {
		return goBean;
	}
	public void setGoBean(GroupOwnerFlat goBean) {
		this.goBean = goBean;
	}
	
	public Long getGroupOwnerID() {
		return groupOwnerID;
	}
	public void setGroupOwnerID(Long groupOwnerID) {
		this.groupOwnerID = groupOwnerID;
	}
	
	public List<GroupBean> getGroupList() {
		return groupList;
	}
	public void setGroupList(List<GroupBean> groupList) {
		this.groupList = groupList;
	}
	
	public List<User> getUserList() {
		return userList;
	}
	public void setUserList(List<User> userList) {
		this.userList = userList;
	}
	
	public GroupOwner getOwner() {
		return owner;
	}
	public void setOwner(GroupOwner owner) {
		this.owner = owner;
	}
}
