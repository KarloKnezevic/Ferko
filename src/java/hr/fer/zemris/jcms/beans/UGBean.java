package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.model.User;

public class UGBean {

	private String username;
	private String firstName;
	private String lastName;
	private String courseName;
	private String groupName;
	private Long userId;
	private Long groupId;
	
	public UGBean() {
		
	}
	
	public UGBean(User user) {
		setUserId(user.getId());
		setUsername(user.getUsername());
		setFirstName(user.getFirstName());
		setLastName(user.getLastName());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	
	
	
}
