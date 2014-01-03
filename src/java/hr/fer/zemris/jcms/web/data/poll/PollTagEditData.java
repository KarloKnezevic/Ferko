package hr.fer.zemris.jcms.web.data.poll;

import java.util.HashMap;
import java.util.Map;

import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class PollTagEditData extends AbstractActionData {
	
	private PollTag pollTag = new PollTag();
	private Map<String, String> errors = new HashMap<String, String>();

	public PollTagEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public PollTag getPollTag() {
		return pollTag;
	}

	public void setPollTag(PollTag pollTag) {
		this.pollTag = pollTag;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, String> errors) {
		this.errors = errors;
	}

	
}
