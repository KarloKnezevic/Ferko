package hr.fer.zemris.jcms.web.data.poll;

import hr.fer.zemris.jcms.service.PollBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.PollForm;

public class PollAnswerData extends PollEditData {
	
	private Long pollUserId;
	private PollForm pollForm;
	private PollBean poll;

	public PollAnswerData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public String getForm() {
		return pollForm.getHtml();
	}

	public Long getPollUserId() {
		return pollUserId;
	}

	public void setPollUserId(Long pollUserId) {
		this.pollUserId = pollUserId;
	}

	public PollBean getPoll() {
		return poll;
	}

	public void setPoll(PollBean poll) {
		this.poll = poll;
	}

	public PollForm getPollForm() {
		return pollForm;
	}

	public void setPollForm(PollForm pollForm) {
		this.pollForm = pollForm;
	}
	

}
