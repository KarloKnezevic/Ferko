package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.StudentApplicationBean;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationAdminAproveData extends BaseCourseInstance {
	
	private String courseInstanceID;
	private Long studentID;
	private User student;
	private List<StudentApplicationBean> beans = null; 
	private Map<String, String> statuses = null;
	private Long fromDefinitionID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationAdminAproveData(IMessageLogger messageLogger) {
		super(messageLogger);
		beans = new ArrayList<StudentApplicationBean>();
		statuses = new HashMap<String, String>();
		for(ApplicationStatus as : ApplicationStatus.values()){
			statuses.put(as.name(), messageLogger.getText(as.name()));
		}
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public Long getStudentID() {
		return studentID;
	}

	public void setStudentID(Long studentID) {
		this.studentID = studentID;
	}

	public List<StudentApplicationBean> getBeans() {
		return beans;
	}

	public void setBeans(List<StudentApplicationBean> beans) {
		this.beans = beans;
	}

	public Map<String, String> getStatuses() {
		return statuses;
	}

	public void setStatuses(Map<String, String> statuses) {
		this.statuses = statuses;
	}

	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	public Long getFromDefinitionID() {
		return fromDefinitionID;
	}
	
	public void setFromDefinitionID(Long fromDefinitionID) {
		this.fromDefinitionID = fromDefinitionID;
	}
	
}
