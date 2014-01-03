package hr.fer.zemris.jcms.web.interceptors.data;

import java.util.Set;

public interface CurrentUser {
	public Long getUserID();
	public String getJmbag();
	public String getUsername();
	public String getFirstName();
	public String getLastName();
	public Set<String> getUserRoles();
	public boolean isUserInRole(String role);
}
