package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.StaffUsersListJSON;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link StaffUsersListJSON}.
 *  
 * @author marcupic
 *
 */
public class StaffUsersListJSONData extends AbstractActionData {

	List<User> users;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public StaffUsersListJSONData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
