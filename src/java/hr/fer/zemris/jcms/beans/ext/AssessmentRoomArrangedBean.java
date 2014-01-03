package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.User;

public class AssessmentRoomArrangedBean {
	
	private Long assessmentRoomID;
	private String roomName;
	private int capacity;
	private int assistantNum;
	private int assistantRequired;
	private int userNum;
	private User firstUser;
	private User lastUser;
	
	public User getFirstUser() {
		return firstUser;
	}
	public void setFirstUser(User firstUser) {
		this.firstUser = firstUser;
	}
	public User getLastUser() {
		return lastUser;
	}
	public void setLastUser(User lastUser) {
		this.lastUser = lastUser;
	}
	public Long getAssessmentRoomID() {
		return assessmentRoomID;
	}
	public void setAssessmentRoomID(Long assessmentRoomID) {
		this.assessmentRoomID = assessmentRoomID;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getAssistantNum() {
		return assistantNum;
	}
	public void setAssistantNum(int assistantNum) {
		this.assistantNum = assistantNum;
	}
	public int getUserNum() {
		return userNum;
	}
	public void setUserNum(int userNum) {
		this.userNum = userNum;
	}
	public int getAssistantRequired() {
		return assistantRequired;
	}
	public void setAssistantRequired(int assistantRequired) {
		this.assistantRequired = assistantRequired;
	}
	
}
