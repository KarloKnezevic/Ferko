package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.CoursePollOverviewData;

@WebClass(dataClass=CoursePollOverviewData.class)
public class CoursePollOverviewAction extends Ext2ActionSupport<CoursePollOverviewData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String viewPollsWithResults() {
		PollService.getCoursePollOverviewData(getEntityManager(), getData());
		return null;
	}
	
	public void setCourseInstanceID(String id) {
		getData().setCourseInstanceID(id);
	}
}
