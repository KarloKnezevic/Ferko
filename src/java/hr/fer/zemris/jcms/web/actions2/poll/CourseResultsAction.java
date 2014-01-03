package hr.fer.zemris.jcms.web.actions2.poll;

import java.util.Set;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.CoursePollResults;

@WebClass(dataClass=CoursePollResults.class)
public class CourseResultsAction extends Ext2ActionSupport<CoursePollResults> {

	private static final long serialVersionUID = 1L;
	
	@WebMethodInfo
	public String execute() {
		return viewResults();
	}

	@WebMethodInfo
	public String viewResults() {
		PollService.getCoursePollResults(getEntityManager(), getData());
		return null;
	}
	
	@WebMethodInfo
	public String viewSinglePollResults() {
		PollService.getSinglePollResults(getEntityManager(), getData());
		return null;
	}
	public void setId(Long id) {
		getData().setId(id);
	}
	
	public void setApid(Long id) {
		getData().setAnsweredPollId(id);
	}
	
	public void setGroup(String[] groups) {
		data.setShowGroups(groups);
	}
	
	public String[] getGroup() {
		return data.getShowGroups();
	}
	
	public void setCourseInstanceID(String courseInstanceID) {
		getData().setCourseInstanceID(courseInstanceID);
	}
	
	public String getCourseInstanceID() {
		return getData().getCourseInstanceID();
	}

	public Set<Long> getSelected() {
		return data.getSelected();
	}
}
