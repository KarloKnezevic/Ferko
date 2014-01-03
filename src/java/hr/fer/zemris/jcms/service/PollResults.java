package hr.fer.zemris.jcms.service;

import java.util.List;

import hr.fer.zemris.jcms.beans.PollQuestionBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.poll.AnsweredPoll;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class PollResults extends AbstractActionData {

	private Poll poll;
	private long id;
	private String[] showGroups;
	private Long answeredPollId;
	private List<PollQuestionBean> questions;
	private List<Group> groups;
	private AnsweredPoll[] answeredPollNeighbours;
	
	public PollResults(IMessageLogger messageLogger) {
		super(messageLogger);
		// TODO Auto-generated constructor stub
	}
	
	public void setPoll(Poll poll) {
		this.poll = poll;
	}
	
	public Poll getPoll() {
		return poll;
	}

	public List<PollQuestionBean> getQuestions() {
		return questions;
	}

	public void setQuestions(List<PollQuestionBean> questions) {
		this.questions = questions;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String[] getShowGroups() {
		return showGroups;
	}

	public void setShowGroups(String[] showGroups) {
		this.showGroups = showGroups;
	}

	public void setAnsweredPollId(Long answeredPollId) {
		this.answeredPollId = answeredPollId;
	}

	public Long getAnsweredPollId() {
		return answeredPollId;
	}

	public AnsweredPoll[] getAnsweredPollNeighbours() {
		return answeredPollNeighbours;
	}

	public void setAnsweredPollNeighbours(AnsweredPoll[] answeredPollNeighbours) {
		this.answeredPollNeighbours = answeredPollNeighbours;
	}

	public AnsweredPoll getPrevAnsweredPoll() {
		if(getAnsweredPollNeighbours()==null) return null;
		return answeredPollNeighbours[0];
	}
	
	public AnsweredPoll getNextAnsweredPoll() {
		if(getAnsweredPollNeighbours()==null) return null;
		return answeredPollNeighbours[1];
	}
}
