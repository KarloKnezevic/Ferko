package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.security.GroupSupportedPermission;

public class UGPBean {
	private Long userID;
	private String jmbag;
	private String lastName;
	private String firstName;
	private Long ugID;
	private String tag;
	private Long currentGroupID;
	private String groupName;
	private Long mpID;
	private GroupSupportedPermission perm;
	
	public UGPBean() {
	}
	
	public UGPBean(Long ugID, Long userID, String jmbag, String firstName,
			String lastName, Long currentGroupID, String groupName, String tag,
			Long mpID, GroupSupportedPermission perm) {
		super();
		this.ugID = ugID;
		this.userID = userID;
		this.jmbag = jmbag;
		this.firstName = firstName;
		this.lastName = lastName;
		this.currentGroupID = currentGroupID;
		this.groupName = groupName;
		this.tag = tag;
		this.mpID = mpID;
		this.perm = perm;
	}

	public GroupSupportedPermission getPerm() {
		return perm;
	}
	public void setPerm(GroupSupportedPermission perm) {
		this.perm = perm;
	}
	
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public Long getUgID() {
		return ugID;
	}
	public void setUgID(Long ugID) {
		this.ugID = ugID;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getCurrentGroupID() {
		return currentGroupID;
	}
	public void setCurrentGroupID(Long currentGroupID) {
		this.currentGroupID = currentGroupID;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Long getMpID() {
		return mpID;
	}
	public void setMpID(Long mpID) {
		this.mpID = mpID;
	}
	
}
