package hr.fer.zemris.jcms.web.data.poll;


import java.util.List;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class CoursePollData extends AbstractActionData {
	
	private List<Group> lectureGroups;
	private List<Group> labGroups;
	private List<Group> privateGroups;
	private String[] selectedGroups;
	private String courseInstanceId;
	private CourseInstance courseInstance;
	private Long id;

	public CoursePollData(IMessageLogger messageLogger) {
		super(messageLogger);
	}


	public String[] getSelectedGroups() {
		return selectedGroups;
	}

	public void setSelectedGroups(String[] selectedGroups) {
		this.selectedGroups = selectedGroups;
	}

	public String getCourseInstanceId() {
		return courseInstanceId;
	}

	public void setCourseInstanceId(String courseInstanceId) {
		this.courseInstanceId = courseInstanceId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}


	public void setLectureGroups(List<Group> lectureGroups) {
		this.lectureGroups = lectureGroups;
	}


	public List<Group> getLectureGroups() {
		return lectureGroups;
	}


	public void setPrivateGroups(List<Group> privateGroups) {
		this.privateGroups = privateGroups;
	}


	public List<Group> getPrivateGroups() {
		return privateGroups;
	}


	public void setLabGroups(List<Group> labGroups) {
		this.labGroups = labGroups;
	}


	public List<Group> getLabGroups() {
		return labGroups;
	}
	

}
