package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.ApplicationDefinition;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApplicationListStudentsData extends BaseCourseInstance {
	
	private ApplicationDefinition definition;
	private List<User> users;
	private Map<Long, StudentApplication> applications; 
	private String courseInstanceID;
	private Long definitionID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationListStudentsData(IMessageLogger messageLogger) {
		super(messageLogger);
		List<User> l = Collections.emptyList();
		Map<Long, StudentApplication> m = Collections.emptyMap();
		setUsers(l);
		setApplications(m);
		
	}
	
	/**
	 * Podaci o primjerku kolegija.
	 * @return primjerak kolegija
	 */
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Map<Long, StudentApplication> getApplications() {
		return applications;
	}

	public void setApplications(Map<Long, StudentApplication> applications) {
		this.applications = applications;
	}

	public ApplicationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ApplicationDefinition definition) {
		this.definition = definition;
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
    public Long getDefinitionID() {
		return definitionID;
	}

	public void setDefinitionID(Long definitionID) {
		this.definitionID = definitionID;
	}

}
