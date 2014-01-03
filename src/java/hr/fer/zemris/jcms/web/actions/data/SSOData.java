package hr.fer.zemris.jcms.web.actions.data;

import java.util.Set;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class SSOData extends AbstractActionData {

	private String username;
	private Long userID;
	private String lastName;
	private String firstName;
	private Set<String> roles;
	private CourseInstance courseInstance;
	
	private boolean valid;
	
	public SSOData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
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

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public Set<String> getRoles() {
		return roles;
	}
	
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	
}
