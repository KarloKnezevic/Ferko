package hr.fer.zemris.jcms.web.interceptors.data;

import java.util.Set;

public class CurrentUserImpl1 implements CurrentUser {

	private Long userID;
	private String jmbag;
	private String username;
	private String firstName;
	private String lastName;
	private Set<String> roles;
	
	public CurrentUserImpl1(Long userID, String username, String firstName, String lastName, String jmbag, Set<String> roles) {
		super();
		this.userID = userID;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.jmbag = jmbag;
		this.roles = roles;
	}

	public String getJmbag() {
		return jmbag;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public Long getUserID() {
		return userID;
	}
	public String getUsername() {
		return username;
	}
	@Override
	public Set<String> getUserRoles() {
		return roles;
	}
	@Override
	public boolean isUserInRole(String role) {
		return roles.contains(role);
	}
	@Override
	public String toString() {
		return "{ current user is: "+userID+"}";
	}
	
}
