package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.CourseUsersListJSON;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link CourseUsersListJSON}.
 *  
 * @author marcupic
 *
 */
public class CourseUsersListJSONData extends BaseCourseInstance {

	List<User> users;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseUsersListJSONData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
