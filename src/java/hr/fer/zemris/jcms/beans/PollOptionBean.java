package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.model.poll.Option;
import hr.fer.zemris.jcms.model.poll.Question;

public class PollOptionBean extends Option implements Comparable<PollOptionBean> {

	private static final long serialVersionUID = 1L;
	
	private long counter;
	
	public PollOptionBean(Option option, Question question, long counter) {
		setId(option.getId());
		setOrdinal(option.getOrdinal());
		setText(option.getText());
		setQuestion(question);
		setCounter(counter);
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}
	

	public long getCounter() {
		return counter;
	}
	
	@Override
	public int compareTo(PollOptionBean o) {
		return getOrdinal().compareTo(o.getOrdinal());
	}

}
