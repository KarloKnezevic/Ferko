package hr.fer.zemris.jcms.web.data.poll;

import java.util.List;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class CoursePollOverviewData extends AbstractActionData {

	private String courseInstanceID;
	private CourseInstance courseInstance;
	private List<Poll> polls;
	
	public CoursePollOverviewData(IMessageLogger messageLogger) {
		super(messageLogger);
		// TODO Auto-generated constructor stub
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public List<Poll> getPolls() {
		return polls;
	}

	public void setPolls(List<Poll> polls) {
		this.polls = polls;
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	
	
}
