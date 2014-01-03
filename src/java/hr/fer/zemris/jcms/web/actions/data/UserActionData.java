package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class UserActionData extends AbstractActionData {

	private List<Role> availableRoles;
	private List<AuthType> availableAuthTypes;
	private User user;
	private boolean canViewAll;
	
	public UserActionData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<AuthType> getAvailableAuthTypes() {
		return availableAuthTypes;
	}
	public void setAvailableAuthTypes(List<AuthType> availableAuthTypes) {
		this.availableAuthTypes = availableAuthTypes;
	}
	
	public List<Role> getAvailableRoles() {
		return availableRoles;
	}
	public void setAvailableRoles(List<Role> availableRoles) {
		this.availableRoles = availableRoles;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean getCanViewAll() {
		return canViewAll;
	}
	public void setCanViewAll(boolean canViewAll) {
		this.canViewAll = canViewAll;
	}
}
