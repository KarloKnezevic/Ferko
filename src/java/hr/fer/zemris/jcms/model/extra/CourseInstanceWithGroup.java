package hr.fer.zemris.jcms.model.extra;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;

public class CourseInstanceWithGroup {

	private CourseInstance courseInstance;
	private Group group;
	
	public CourseInstanceWithGroup() {
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
}
