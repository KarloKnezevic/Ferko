package hr.fer.zemris.jcms.web.data.poll;

import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.service.PollBean;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class PollIndexData extends AbstractActionData {
	
	public PollIndexData(IMessageLogger messageLogger) {
		super(messageLogger);
		// TODO Auto-generated constructor stub
	}
	private List<PollBean> unansweredPolls;
	private List<Poll> pollResults;
	
	public List<PollBean> getUnansweredPolls() {
		return unansweredPolls;
	}
	public void setUnansweredPolls(List<PollBean> unansweredPolls) {
		this.unansweredPolls = unansweredPolls;
	}
	public List<Poll> getPollResults() {
		return pollResults;
	}
	public void setPollResults(List<Poll> pollResults) {
		this.pollResults = pollResults;
	}
	



	
}
