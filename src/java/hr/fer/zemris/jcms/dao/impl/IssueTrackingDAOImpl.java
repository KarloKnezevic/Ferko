package hr.fer.zemris.jcms.dao.impl;

import java.util.List;

import hr.fer.zemris.jcms.dao.IssueTrackingDAO;
import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueAnswer;
import hr.fer.zemris.jcms.model.IssueAssistantVersion;
import hr.fer.zemris.jcms.model.IssueTopic;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public class IssueTrackingDAOImpl implements IssueTrackingDAO {
	 
	@Override
	public Long checkIssueUpdates(EntityManager em, String courseInstanceID) {
		return (Long)em.createNamedQuery("Issue.updateCheckStaff")
		.setParameter("courseInstanceID", courseInstanceID)
		.getSingleResult();
	}
	
	@Override
	public Long checkIssueUpdatesStaff(EntityManager em, String courseInstanceID, Long userID) {
		return (Long)em.createNamedQuery("Issue.updateCheckStaff2")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID", userID)
		.getSingleResult();
	}
	
	@Override
	public Long checkIssueUpdates(EntityManager em, String courseInstanceID, Long userID) {
		return (Long)em.createNamedQuery("Issue.updateCheckStudent").
		setParameter("courseInstanceID", courseInstanceID)
		.setParameter("ownerID", userID)
		.getSingleResult();
	}
	
	@Override
	public void saveTopic(EntityManager em, IssueTopic topic) {
		em.persist(topic);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IssueTopic findTopic(EntityManager em, String topicName, String courseInstanceID) {
		List<IssueTopic> topics = (List<IssueTopic>)em.createNamedQuery("IssueTopic.findTopic")
		.setParameter("topicName", topicName)
		.setParameter("courseInstanceID", courseInstanceID)
		.getResultList();
		if(topics==null || topics.isEmpty()) return null;
		else return topics.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IssueTopic> listCourseTopics(EntityManager em, String courseInstanceID, String activityFilter) {
		List<IssueTopic> topics = null;
		if(activityFilter.equals("ACTIVE_TOPICS_ONLY")){
			topics = (List<IssueTopic>)em.createNamedQuery("IssueTopic.listActiveForCourseInstance")
			.setParameter("courseInstanceID", courseInstanceID)
			.getResultList();
		}else{
			topics = (List<IssueTopic>)em.createNamedQuery("IssueTopic.listForCourseInstance")
			.setParameter("courseInstanceID", courseInstanceID)
			.getResultList();
		}
		if(topics==null || topics.isEmpty()) return null;
		else return topics;
	}

	@Override
	public IssueTopic getTopicByID(EntityManager em, Long topicID) {
		try{
			return (IssueTopic)em.createNamedQuery("IssueTopic.getTopicByID")
			.setParameter("topicID", topicID)
			.getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
	}

	@Override
	public void save(EntityManager em, Issue newMessage) {
		em.persist(newMessage);
	}

	@Override
	public Issue get(EntityManager em, Long issueID) {
		return (Issue)em.createNamedQuery("Issue.getByID")
		.setParameter("messageID", issueID)
		.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IssueAnswer> getAnswersForIssue(EntityManager em, Long issueID) {
		List<IssueAnswer> answers = null;
		answers = (List<IssueAnswer>)em.createNamedQuery("IssueAnswer.listForIssue")
		.setParameter("issueID", issueID)
		.getResultList();
		if(answers==null || answers.isEmpty()) return null;
		else return answers;
	}

	@Override
	public void sendAnswer(EntityManager em, IssueAnswer answer) {
		em.persist(answer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listActiveForAsistent(EntityManager em, String courseInstanceID, Long userID) {
		List<Issue> msgs = null;
		msgs = (List<Issue>)em.createNamedQuery("Issue.listActiveForAsistent2")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID",userID)
		.getResultList();
		if(msgs==null || msgs.isEmpty()) return null;
		else return msgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listActiveForStudent(EntityManager em, Long userID, String courseInstanceID) {
		List<Issue> msgs = null;
		msgs = (List<Issue>)em.createNamedQuery("Issue.listActiveForStudent")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID", userID)
		.getResultList();
		if(msgs==null || msgs.isEmpty()) return null;
		else return msgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listResolvedForAsistent(EntityManager em, String courseInstanceID, Long userID) {
		List<Issue> msgs = null;
		msgs = (List<Issue>)em.createNamedQuery("Issue.listResolvedForAsistent2")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID", userID)
		.getResultList();
		if(msgs==null || msgs.isEmpty()) return null;
		else return msgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listResolvedForStudent(EntityManager em, Long userID, String courseInstanceID) {
		List<Issue> msgs = null;
		msgs = (List<Issue>)em.createNamedQuery("Issue.listResolvedForStudent")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID", userID)
		.getResultList();
		if(msgs==null || msgs.isEmpty()) return null;
		else return msgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listIssuesForActivation(EntityManager em, String courseInstanceID) {
		List<Issue> msgs = null;
		msgs = (List<Issue>)em.createNamedQuery("Issue.listPostponedForActivation")
		.setParameter("courseInstanceID", courseInstanceID)
		.getResultList();
		return msgs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> listAssistantsWithVersions(EntityManager em, String courseInstanceID) {
		List<Long> assistants = (List<Long>)em.createNamedQuery("Issue.listAssistantsWithVersions") 
								.setParameter("courseInstanceID", courseInstanceID)
								.getResultList();
		if(assistants==null || assistants.isEmpty()) return null;
		else return assistants;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> listIssuesOnCourse(EntityManager em, String courseInstanceID) {
		List<Issue> issues = (List<Issue>)em.createNamedQuery("Issue.listIssuesOnCourse") 
							 .setParameter("courseInstanceID", courseInstanceID)
							 .getResultList();
		if(issues==null || issues.isEmpty()) return null;
		else return issues;
	}

	@Override
	public void saveIssueVersion(EntityManager em, IssueAssistantVersion iav) {
		em.persist(iav);
	}
	
}
