package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.PollUser;

public class PollBean extends Poll {

	private static final long serialVersionUID = 1L;

	private PollUser pollUser;

	public PollBean(Poll poll) {
		setTitle(poll.getTitle());
		setDescription(poll.getDescription());
		setId(poll.getId());
		setStartDate(poll.getStartDate());
		setEndDate(poll.getEndDate());
	}

	public PollUser getPollUser() {
		return pollUser;
	}

	public void setPollUser(PollUser pollUser) {
		this.pollUser = pollUser;
	}
	
	
}
