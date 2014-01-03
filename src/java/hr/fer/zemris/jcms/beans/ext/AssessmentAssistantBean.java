package hr.fer.zemris.jcms.beans.ext;

public class AssessmentAssistantBean {
	String userID;
	String firstName;
	String lastName;
	String jmbag;
	String taken;
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getTaken() {
		return taken;
	}
	public void setTaken(String taken) {
		this.taken = taken;
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
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}
}
