package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.service2.course.CourseService.GroupLecturers;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link ShowCourse}.
 *  
 * @author marcupic
 *
 */
public class ShowCourseData extends BaseCourseInstance {

	private String courseInstanceID;
	private boolean renderCourseAdministration;
	private boolean newIssues;
	private Set<String> administrationPermissions;
	private List<GroupLecturers> lecturers;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ShowCourseData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public boolean getRenderCourseAdministration() {
		return renderCourseAdministration;
	}
	public void setRenderCourseAdministration(boolean renderCourseAdministration) {
		this.renderCourseAdministration = renderCourseAdministration;
	}

	public boolean getNewIssues() {
		return newIssues;
	}

	public void setNewIssues(boolean newIssues) {
		this.newIssues = newIssues;
	}
	
	public Set<String> getAdministrationPermissions() {
		return administrationPermissions;
	}
	public void setAdministrationPermissions(
			Set<String> administrationPermissions) {
		this.administrationPermissions = administrationPermissions;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public List<GroupLecturers> getLecturers() {
		return lecturers;
	}
	public void setLecturers(List<GroupLecturers> lecturers) {
		this.lecturers = lecturers;
	}
}
