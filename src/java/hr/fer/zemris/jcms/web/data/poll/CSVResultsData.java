package hr.fer.zemris.jcms.web.data.poll;

import java.util.List;

import hr.fer.zemris.jcms.model.poll.Answer;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class CSVResultsData extends AbstractActionData {
	
	private List<Answer> answers;
	private Long id;
	private String courseInstanceID;

	public CSVResultsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}



	public List<Answer> getAnswers() {
		return answers;
	}



	public void setAnswers(List<Answer> answers) {
		this.answers = answers;
	}

}
