package hr.fer.zemris.jcms.web.data.poll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class PollEditData extends AbstractActionData {
	
	private Long id;
	private CourseInstance courseInstance;
	private Map<String, String> errors = new HashMap<String, String>();
	private String startDate;
	private String endDate;
	private String startTime;
	private String endTime;
	private String title;
	private String description;
	private String JSONDescriptionOfQuestions;
	private boolean canTag = false;
	private String pollTagId;
	private List<PollTag> pollTags;

	public PollEditData(IMessageLogger messageLogger) {
		super(messageLogger);
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, String> errors) {
		this.errors = errors;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJSONDescriptionOfQuestions() {
		return JSONDescriptionOfQuestions;
	}

	public void setJSONDescriptionOfQuestions(String descriptionOfQuestions) {
		JSONDescriptionOfQuestions = descriptionOfQuestions;
	}

	public boolean isCanTag() {
		return canTag;
	}

	public void setCanTag(boolean canTag) {
		this.canTag = canTag;
	}

	public String getPollTagId() {
		return pollTagId;
	}

	public void setPollTagId(String pollTagId) {
		this.pollTagId = pollTagId;
	}

	public List<PollTag> getPollTags() {
		return pollTags;
	}

	public void setPollTags(List<PollTag> pollTags) {
		this.pollTags = pollTags;
	}


	
}
