package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class UserImportData extends AbstractActionData {

	private List<AuthType> allAuthTypes;
	private List<Role> allRoles;
	
	public UserImportData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<AuthType> getAllAuthTypes() {
		return allAuthTypes;
	}

	public void setAllAuthTypes(List<AuthType> allAuthTypes) {
		this.allAuthTypes = allAuthTypes;
	}

	public List<Role> getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(List<Role> allRoles) {
		this.allRoles = allRoles;
	}

}
