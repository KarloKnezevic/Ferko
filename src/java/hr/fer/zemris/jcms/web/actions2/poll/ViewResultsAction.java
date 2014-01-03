package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service.PollResults;
import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;

@WebClass(dataClass=PollResults.class)
public class ViewResultsAction extends Ext2ActionSupport<PollResults> {

	private static final long serialVersionUID = 1L;
	
	@WebMethodInfo
	public String execute() {
		return viewResults();
	}

	@WebMethodInfo
	public String viewResults() {
		PollService.getPollResults(getEntityManager(), getData());
		return null;
	}
	
	@WebMethodInfo
	public String viewSinglePollResults() {
		//PollService.getSinglePollResults(getEntityManager(), getData());
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
}
