package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.poll.Poll;

public class ViewPoll extends ExtendedActionSupport {

	private static final long serialVersionUID = 1L;
	private String pollId = null;
	@SuppressWarnings("unused")
	private Long userId = null;
	private Poll poll = null;
	
	public String execute() {
		//TODO: Autorizacija, trenutno provjerava je li anketa za tu osobu
		if(!hasCurrentUser()) return NO_PERMISSION;
		if(pollId==null) return NO_PERMISSION; // TODO: treba vratiti 404
		/*
		userId = getCurrentUser().getUserID();
    	poll = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Poll>() {
			public Poll executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				Poll poll = null;
				try {
					poll = dh.getPollDAO().getPollForOwner(em, Long.parseLong(pollId), userId);
				} catch (NoResultException e) {
					poll=null;
				}
				if(poll==null) return null;
				poll.getQuestions().size();
				for(Question q : poll.getQuestions()) {
					if(q instanceof SingleChoiceQuestion) ((SingleChoiceQuestion)q).getOptions().size();
					if(q instanceof MultiChoiceQuestion) ((MultiChoiceQuestion)q).getOptions().size();
					if(q instanceof TextQuestion) ((TextQuestion)q).getTextAnswers().size();
				}
				return poll;
			}
    	});
    	if(poll==null) return NO_PERMISSION;
		return SUCCESS;
		*/
		return SUCCESS;
	}
	
	public String getPollId() {
		return pollId;
	}
	public void setPollId(String pollId) {
		this.pollId = pollId;
	}
	public Poll getPoll() {
		return poll;
	}
}
