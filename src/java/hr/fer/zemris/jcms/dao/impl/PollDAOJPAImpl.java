package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.beans.PollOptionBean;
import hr.fer.zemris.jcms.dao.PollDAO;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.poll.Answer;
import hr.fer.zemris.jcms.model.poll.AnsweredPoll;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.model.poll.PollUser;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.TextAnswer;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public class PollDAOJPAImpl implements PollDAO {

	@SuppressWarnings("unchecked")
	public List<Poll> all(EntityManager em) {
		return (List<Poll>)em.createNamedQuery("Poll.all").getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Poll> getEditablePolls(EntityManager em) {
		return (List<Poll>)em.createNamedQuery("Poll.editable").getResultList();
	}

	public Poll getPoll(EntityManager em, long id) {
		return (Poll)em.createNamedQuery("Poll.byId")
		.setParameter("id", id).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<Poll> getPollsForUser(EntityManager em, long user) {
		return (List<Poll>)em.createNamedQuery("Poll.toAnswer")
		.setParameter("user", user).getResultList();
	}
	@SuppressWarnings("unchecked")
	public List<PollUser> getUnanswerdPUsForUser(EntityManager em, long user) {
		return (List<PollUser>)em.createNamedQuery("PollUser.toAnswer")
		.setParameter("user", user).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Poll> getPollsWithName(EntityManager em, String title) {
		return (List<Poll>)em.createNamedQuery("Poll.byName")
		.setParameter("name", title).getResultList();
	}

	public void remove(EntityManager em, Poll p) {
		for(Question q : p.getQuestions()) { // jednostavnije brisanje?
			em.createNamedQuery("Poll.removeAnswersForQuestion").setParameter("question", q).executeUpdate();
		}
		em.createNamedQuery("Poll.removeAnsweredPolls").setParameter("id", p.getId()).executeUpdate();
		em.remove(p);
	}

	public void save(EntityManager em, Poll p) {
		if(p.getId()==null) {
			em.persist(p);
		} else {
			Poll p2 = (Poll)em.find(Poll.class, p.getId());
			p.setVersion(p2.getVersion());
			em.merge(p);
		}

	}

	public Poll getPollForUser(EntityManager em, Long pollId, Long userId) {
		return (Poll)em.createNamedQuery("Poll.forUserAndPoll")
		.setParameter("pollId", pollId).setParameter("userId", userId).getSingleResult();
	}

	public Poll getPollForOwner(EntityManager em, long pollId, Long userId) {
		return (Poll)em.createNamedQuery("Poll.forOwnerAndPoll")
		.setParameter("pollId", pollId).setParameter("userId", userId).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<Poll> getPollsForOwner(EntityManager em, Long userID) {
		return (List<Poll>)em.createNamedQuery("Poll.byOwnerId")
		.setParameter("userId", userID).getResultList();
	}

	public void savePollUser(EntityManager em, PollUser u) {
		if(u.getId()==null) {
			em.persist(u);
		} else {
			PollUser p2 = (PollUser)em.find(PollUser.class, u.getId());
			u.setVersion(p2.getVersion());
			em.merge(u);
		}		
	}

	@Override
	public PollUser getPollUser(EntityManager em, Long id) {
		return (PollUser)em.createNamedQuery("PollUser.byId")
		.setParameter("id", id).getSingleResult();
	}

	@Override
	public void saveAnswer(EntityManager em, Answer answer) {
		if(answer.getId()==null) {
			em.persist(answer);
		} else {
			PollUser p2 = (PollUser)em.find(PollUser.class, answer.getId());
			answer.setVersion(p2.getVersion());
			em.merge(answer);
		}	
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Long id) {
		return (List<TextAnswer>)em.createNamedQuery("TextAnswer.getAll")
		.setParameter("id", id).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em, Long id) {
		return (List<PollOptionBean>)em.createNamedQuery("OptionAnswer.countAll")
		.setParameter("id", id).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getAllGroupsForPoll(EntityManager em, Long id) {
		return (List<Group>)em.createNamedQuery("Poll.getGroupsForPoll")
		.setParameter("id", id).getResultList();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em,
			Poll poll, List<Group> pollGroups) {
		if(pollGroups.size()==0) return new ArrayList<PollOptionBean>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT NEW hr.fer.zemris.jcms.beans.PollOptionBean(o, o.question, COUNT(a)) " +
		          "FROM OptionAnswer a RIGHT JOIN a.option o WHERE o.question.poll.id = ").append(poll.getId()).append(" AND (");
		sb.append(" a.answeredPoll.group.id IN (").append("'").append(pollGroups.get(0).getId()).append("'");
		for(int k=1; k<pollGroups.size(); k++) sb.append(",'").append(pollGroups.get(k).getId()).append("'");
		sb.append(") OR a IS NULL) GROUP BY o, o.question");
		return (List<PollOptionBean>)em.createQuery(sb.toString()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Poll poll,
			List<Group> pollGroups) {
		if(pollGroups.size()==0) return new ArrayList<TextAnswer>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT a FROM TextAnswer a JOIN FETCH a.answeredPoll JOIN FETCH a.question "); 
		sb.append(" WHERE a.question.poll.id = ").append(poll.getId()).append(" AND ");
		sb.append(" a.answeredPoll.group.id IN (").append("'").append(pollGroups.get(0).getId()).append("'");
		for(int k=1; k<pollGroups.size(); k++) sb.append(",'").append(pollGroups.get(k).getId()).append("'");
		sb.append(")");
		return (List<TextAnswer>)em.createQuery(sb.toString()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Answer> getAllAnswers(EntityManager em, Poll poll,
			List<Group> pollGroups) {
		if(pollGroups.size()==0) return new ArrayList<Answer>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT a FROM Answer a JOIN FETCH a.answeredPoll JOIN FETCH a.question JOIN FETCH a.answeredPoll.group"); 
		sb.append(" WHERE a.question.poll.id = ").append(poll.getId()).append(" AND ");
		sb.append(" a.answeredPoll.group.id IN (").append("'").append(pollGroups.get(0).getId()).append("'");
		for(int k=1; k<pollGroups.size(); k++) sb.append(",'").append(pollGroups.get(k).getId()).append("'");
		sb.append(")");
		return (List<Answer>)em.createQuery(sb.toString()).getResultList();
	}

	@Override
	public void saveAnsweredPoll(EntityManager em, AnsweredPoll ap) {
		if(ap.getId()==null) {
			em.persist(ap);
		} else {
			AnsweredPoll ap2 = (AnsweredPoll)em.find(AnsweredPoll.class, ap.getId());
			ap.setVersion(ap2.getVersion());
			em.merge(ap2);
		}
	}

	@Override
	public AnsweredPoll getAnsweredPoll(EntityManager em, Long id) {
		AnsweredPoll ap = (AnsweredPoll)em.find(AnsweredPoll.class, id);
		return ap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em,
			Long id, AnsweredPoll ap) {
		return (List<PollOptionBean>)em.createNamedQuery("OptionAnswer.countAllForAnsweredPoll")
		.setParameter("id", id).setParameter("ap", ap).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Long id,
			AnsweredPoll ap) {
		return (List<TextAnswer>)em.createNamedQuery("TextAnswer.getAllForAnsweredPoll")
		.setParameter("id", id).setParameter("ap", ap).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<AnsweredPoll> getAnsweredPollsForGroupOwner(EntityManager em,
			Poll poll, User owner) {
		return (Set<AnsweredPoll>)em.createNamedQuery("Poll.getAnsweredPollsForViewer")
		.setParameter("poll", poll).setParameter("user", owner).getResultList();
	}

	@Override
	public int countAnsweredPollsForGroupOwner(EntityManager em, Poll poll,
			User owner) {
		return (Integer)em.createNamedQuery("Poll.countAnsweredPollsForViewer")
		.setParameter("poll", poll).setParameter("user", owner).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Poll> getAllPollsForView(EntityManager em, User user,
			CourseInstance courseInstance, Set<String> rolesOnCourse) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT pu.poll FROM PollUser pu, GroupOwner go WHERE go.group = pu.group ");
		query.append("AND pu.group.compositeCourseID = :cid AND ( go.user = :user ");
		if(rolesOnCourse.contains(JCMSSecurityConstants.NOSITELJ) ||
		   rolesOnCourse.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA) ||
		   rolesOnCourse.contains(JCMSSecurityConstants.ASISTENT_ORG)) {
			query.append("OR go.group.relativePath LIKE '0/%' "); // grupe za predavanja 
			query.append("OR go.group.relativePath LIKE '1/%' "); // grupe za laboratorijske vježbe 
			query.append("OR go.group.relativePath LIKE '2/%' "); // grupe za domace zadace 
			query.append("OR go.group.relativePath LIKE '4/%' "); // grupe za ispite
			query.append("OR go.group.relativePath LIKE '5/%' "); // grupe za seminare
		} else if(rolesOnCourse.contains(JCMSSecurityConstants.NASTAVNIK)){
			query.append("OR go.group.relativePath LIKE '1/%' "); 
			query.append("OR go.group.relativePath LIKE '2/%' "); 
			query.append("OR go.group.relativePath LIKE '4/%' ");
			query.append("OR go.group.relativePath LIKE '5/%' ");
		}
		query.append(")");
		return (List<Poll>)(em.createQuery(query.toString()).setParameter("cid", courseInstance.getId()).setParameter("user", user).getResultList());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Poll> getAllPollsOnCourse(EntityManager em,
			CourseInstance courseInstance) {
		String query = "SELECT DISTINCT pu.poll FROM PollUser pu WHERE pu.group.compositeCourseID = :cid";		
		return (List<Poll>)(em.createQuery(query).setParameter("cid", courseInstance.getId()).getResultList());
	}

	@Override
	public void removeAllQuestions(EntityManager em, Poll poll) {
		String query = "DELETE FROM Option AS o WHERE o.question = :q";
	
		for(Question q : poll.getQuestions()) {
			em.createQuery(query).setParameter("q", q).executeUpdate();
		}
		
		query = "DELETE FROM Question AS q WHERE q.poll = :poll";
		em.createQuery(query).setParameter("poll", poll).executeUpdate();
	}

	@Override
	public PollTag getPollTag(EntityManager em, Long id) {
		return (PollTag)(em.createQuery("SELECT pt FROM PollTag pt WHERE pt.id = :id").setParameter("id", id).getSingleResult());
	}

	@Override
	public void remove(EntityManager em, PollTag pollTag) {
		em.remove(pollTag);
	}
	
	public void save(EntityManager em, PollTag p) {
		if(p.getId()==null) {
			em.persist(p);
		} else {
			PollTag p2 = (PollTag)em.find(PollTag.class, p.getId());
			p.setVersion(p2.getVersion());
			em.merge(p);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PollTag> getPollTags(EntityManager em) {
		return (List<PollTag>)(em.createQuery("SELECT pt FROM PollTag pt").getResultList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getAllGroupsForPollOnCourse(EntityManager em,
			CourseInstance courseInstance, Poll poll) {
		String query = "SELECT DISTINCT pu.group FROM PollUser pu WHERE pu.poll = :poll AND pu.group.compositeCourseID = :cid";
		return (List<Group>)(em.createQuery(query).setParameter("poll", poll).setParameter("cid", courseInstance.getId()).getResultList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getGroupsWhereUserCanSeeGroupResults(EntityManager em,
			User user, CourseInstance courseInstance, Set<String> rolesOnCourse) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT g FROM Group g, GroupOwner go WHERE go.group = g ");
		query.append("AND g.compositeCourseID = :cid AND ( go.user = :user ");
		if(rolesOnCourse.contains(JCMSSecurityConstants.NOSITELJ) ||
		   rolesOnCourse.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA) ||
		   rolesOnCourse.contains(JCMSSecurityConstants.ASISTENT_ORG)) {
			query.append("OR g.relativePath LIKE '0/%' "); // grupe za predavanja 
			query.append("OR g.relativePath LIKE '1/%' "); // grupe za laboratorijske vježbe 
			query.append("OR g.relativePath LIKE '2/%' "); // grupe za domace zadace 
			query.append("OR g.relativePath LIKE '4/%' "); // grupe za ispite
			query.append("OR g.relativePath LIKE '5/%' "); // grupe za seminare
		} else if(rolesOnCourse.contains(JCMSSecurityConstants.NASTAVNIK)){
			query.append("OR g.relativePath LIKE '1/%' "); 
			query.append("OR g.relativePath LIKE '2/%' "); 
			query.append("OR g.relativePath LIKE '4/%' ");
			query.append("OR g.relativePath LIKE '5/%' ");
		}
		query.append(")");
		return (List<Group>)(em.createQuery(query.toString()).setParameter("cid", courseInstance.getId()).setParameter("user", user).getResultList());
	}
	
	@Override
	public long countAnsweredPolls(EntityManager em, Poll poll,
			List<Group> pollGroups) {
		// TODO: napravi funkciju za stvaranja string id-ova iz liste koji se koristi na par mjesta
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(ap) FROM AnsweredPoll ap WHERE ap.poll = :poll AND ap.group IN (");
		query.append("'").append(pollGroups.get(0).getId()).append("'");
		for(int k=1; k<pollGroups.size(); k++) query.append(",'").append(pollGroups.get(k).getId()).append("'");
		query.append(")");
		return (Long)em.createQuery(query.toString()).setParameter("poll", poll).getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AnsweredPoll[] getAnsweredPollNeighbours(EntityManager em,
			AnsweredPoll ap, List<Group> singlePollResultsGroups) {
		AnsweredPoll[] neighbours = new AnsweredPoll[2];
		if(singlePollResultsGroups.size()==0) return neighbours;
		StringBuilder queryPrev = new StringBuilder();
		StringBuilder queryNext = new StringBuilder();
		StringBuilder groupList = new StringBuilder();
		queryPrev.append("SELECT ap FROM AnsweredPoll ap WHERE ap.id < :apid AND ap.poll = :poll AND ap.group IN ");
		queryNext.append("SELECT ap FROM AnsweredPoll ap WHERE ap.id > :apid AND ap.poll = :poll AND ap.group IN ");
		groupList.append("('").append(singlePollResultsGroups.get(0).getId()).append("'");
		for(int k=1; k<singlePollResultsGroups.size(); k++) groupList.append(",'").append(singlePollResultsGroups.get(k).getId()).append("'");
		groupList.append(")");
		queryPrev.append(groupList);
		queryNext.append(groupList);
		queryPrev.append(" ORDER BY ap.id DESC");
		queryNext.append(" ORDER BY ap.id ASC");
		List<AnsweredPoll> aps = (List<AnsweredPoll>)em.createQuery(queryPrev.toString()).setParameter("apid", ap.getId()).setParameter("poll", ap.getPoll()).setMaxResults(1).getResultList();
		if(aps.size()>0) neighbours[0] = aps.get(0);
		aps = (List<AnsweredPoll>)em.createQuery(queryNext.toString()).setParameter("apid", ap.getId()).setParameter("poll", ap.getPoll()).setMaxResults(1).getResultList();
		if(aps.size()>0) neighbours[1] = aps.get(0);
		return neighbours;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AnsweredPoll getFirstAnsweredPoll(EntityManager em, Poll poll,
			List<Group> singlePollResultsGroups) {
		if(singlePollResultsGroups.size()==0) return null;
		StringBuilder query = new StringBuilder();
		query.append("SELECT ap FROM AnsweredPoll ap WHERE ap.poll = :poll AND ap.group IN ");
		query.append("('").append(singlePollResultsGroups.get(0).getId()).append("'");
		for(int k=1; k<singlePollResultsGroups.size(); k++) query.append(",'").append(singlePollResultsGroups.get(k).getId()).append("'");
		query.append(")");
		query.append(" ORDER BY ap.id ASC");
		List<AnsweredPoll> aps = (List<AnsweredPoll>)em.createQuery(query.toString()).setParameter("poll", poll).setMaxResults(1).getResultList();
		if(aps.size()==0) return null;
		return aps.get(0);
	}
	
}
