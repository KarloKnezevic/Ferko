package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.CoursePollData;

@WebClass(dataClass=CoursePollData.class)
public class CoursePollAction extends Ext2ActionSupport<CoursePollData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String addGroups() {
		if(data.getSelectedGroups() == null) {
			PollService.getCoursePollData(getEntityManager(), getData());
		} else {
			PollService.addGroups(getEntityManager(), getData());
		}
		return null;
	}
	
	public void setCourseInstanceID(String id) {
		data.setCourseInstanceId(id);
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceId();
	}
	
	public void setGroup(String[] groups) {
		data.setSelectedGroups(groups);
	}
	
	public void setId(Long id) {
		data.setId(id);
	}
	
	public Long getId() {
		return data.getId();
	}
}
