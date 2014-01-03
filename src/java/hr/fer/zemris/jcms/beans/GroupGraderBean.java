package hr.fer.zemris.jcms.beans;

public class GroupGraderBean {
	String userID;
	String groupID;
	String group;
	
	public GroupGraderBean() {
	}

	public GroupGraderBean(String userID, String groupID, String group) {
		super();
		this.userID = userID;
		this.groupID = groupID;
		this.group = group;
	}


	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
}
