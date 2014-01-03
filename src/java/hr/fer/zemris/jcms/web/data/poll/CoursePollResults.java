package hr.fer.zemris.jcms.web.data.poll;

import java.util.HashSet;
import java.util.Set;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.service.PollResults;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class CoursePollResults extends PollResults {
	
	private CourseInstance courseInstance;
	private String courseInstanceID;
	private Set<String> administrationPermissions = new HashSet<String>();
	private Set<Long> selected;
	private long answeredPollsCounter = 0;

	public CoursePollResults(IMessageLogger messageLogger) {
		super(messageLogger);
		// TODO Auto-generated constructor stub
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public Set<String> getAdministrationPermissions() {
		return administrationPermissions;
	}

	public void setAdministrationPermissions(Set<String> administrationPermissions) {
		this.administrationPermissions = administrationPermissions;
	}
	
	public Set<Long> getSelected() {
		return selected;
	}
	
	public void setSelected(Set<Long> selected) {
		this.selected = selected;
	}

	public long getAnsweredPollsCounter() {
		return answeredPollsCounter;
	}

	public void setAnsweredPollsCounter(long answeredPollsCounter) {
		this.answeredPollsCounter = answeredPollsCounter;
	}

	
}
