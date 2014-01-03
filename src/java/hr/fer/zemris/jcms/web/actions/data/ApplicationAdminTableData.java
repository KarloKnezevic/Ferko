package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.ApplicationDefinition;

import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApplicationAdminTableData extends BaseCourseInstance {
	
	private List<ApplicationDefinition> definitions;
	private List<User> users;
	private Map <Long, Map<Long, StudentApplication>> applications; 
	private boolean fullList;
	private String courseInstanceID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationAdminTableData(IMessageLogger messageLogger) {
		super(messageLogger);
		List<ApplicationDefinition> l = Collections.emptyList();
		List<User> l2 = Collections.emptyList();
		Map <Long, Map<Long, StudentApplication>> m = Collections.emptyMap();
		setDefinitions(l);
		setUsers(l2);
		setApplications(m);
		
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public List<ApplicationDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ApplicationDefinition> definitions) {
		this.definitions = definitions;
	}
	
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Map <Long, Map<Long, StudentApplication>> getApplications() {
		return applications;
	}

	public void setApplications(Map <Long, Map<Long, StudentApplication>> applications) {
		this.applications = applications;
	}

	public boolean isFullList() {
		return fullList;
	}
	public void setFullList(boolean fullList) {
		this.fullList = fullList;
	}
}
