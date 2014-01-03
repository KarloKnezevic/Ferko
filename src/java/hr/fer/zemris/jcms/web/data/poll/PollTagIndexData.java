package hr.fer.zemris.jcms.web.data.poll;

import java.util.List;

import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class PollTagIndexData extends AbstractActionData {
	
	List<PollTag> pollTags;

	public PollTagIndexData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<PollTag> getPollTags() {
		return pollTags;
	}

	public void setPollTags(List<PollTag> pollTags) {
		this.pollTags = pollTags;
	}

	
}
