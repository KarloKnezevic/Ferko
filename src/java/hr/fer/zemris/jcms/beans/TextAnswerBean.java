package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.model.poll.TextAnswer;

public class TextAnswerBean {

	String answer;
	Long answeredPollId;
	
	public TextAnswerBean(TextAnswer a) {
		answer = a.getAnswer();
		answeredPollId = a.getAnsweredPoll().getId();
	}
	
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public Long getAnsweredPollId() {
		return answeredPollId;
	}
	public void setAnsweredPollId(Long answeredPollId) {
		this.answeredPollId = answeredPollId;
	}
	
	
}
