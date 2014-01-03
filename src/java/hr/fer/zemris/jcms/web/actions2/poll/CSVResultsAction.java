package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.CSVResultsData;


@WebClass(dataClass=CSVResultsData.class)
public class CSVResultsAction extends Ext2ActionSupport<CSVResultsData> {

	private static final long serialVersionUID = 1L;
	
	@WebMethodInfo
	public String execute() {
		PollService.getCSVPollResults(getEntityManager(), getData());
		return null;
	}
	
	public Long getId() {
		return data.getId();
	}
	public void setId(Long id) {
		data.setId(id);
	}
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String id) {
		data.setCourseInstanceID(id);
	}
	
}
