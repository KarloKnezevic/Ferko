package hr.fer.zemris.jcms.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserBean {

	private Long id;
	private String jmbag;
	private String firstName;
	private String lastName;
	private String username;
	private String authUsername;
	private String password;
	private String doublePassword;
	private String email;
	private Long authTypeID;
	private boolean dataValid;
	private boolean locked;
	private List<String> roles;
	private Map<String,Object> preferences;
	
	public UserBean() {
	}
	
	public UserBean(boolean init) {
		if(init) {
			preferences = new HashMap<String, Object>();
		}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
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
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAuthUsername() {
		return authUsername;
	}
	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Long getAuthTypeID() {
		return authTypeID;
	}
	public void setAuthTypeID(Long authTypeID) {
		this.authTypeID = authTypeID;
	}
	public boolean getDataValid() {
		return dataValid;
	}
	public void setDataValid(boolean dataValid) {
		this.dataValid = dataValid;
	}
	public boolean getLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public String getDoublePassword() {
		return doublePassword;
	}
	public void setDoublePassword(String doublePassword) {
		this.doublePassword = doublePassword;
	}
	public Map<String, Object> getPreferences() {
		return preferences;
	}
	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}
}
