package hr.fer.zemris.jcms.dao;

import java.util.List;
import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueAnswer;
import hr.fer.zemris.jcms.model.IssueAssistantVersion;
import hr.fer.zemris.jcms.model.IssueTopic;
import javax.persistence.EntityManager;

public interface IssueTrackingDAO {

	public Long checkIssueUpdates(EntityManager em, String courseInstanceID);
	public Long checkIssueUpdates(EntityManager em, String courseInstanceID, Long userID);
	public void saveTopic(EntityManager em, IssueTopic topic);
	public IssueTopic findTopic(EntityManager em, String topicName, String courseInstanceID);
	public List<IssueTopic> listCourseTopics(EntityManager em, String courseInstanceID, String activityFilter);
	public IssueTopic getTopicByID(EntityManager em, Long topicID);
	public void save(EntityManager em, Issue newIssue);
	public Issue get(EntityManager em, Long issueID);
	public List<IssueAnswer> getAnswersForIssue(EntityManager em, Long issueID);
	public void sendAnswer(EntityManager em, IssueAnswer answer);
	public List<Issue> listActiveForAsistent(EntityManager em, String courseInstanceID, Long userID);
	public List<Issue> listResolvedForAsistent(EntityManager em, String courseInstanceID, Long userID);
	public List<Issue> listActiveForStudent(EntityManager em, Long userID, String courseInstanceID);
	public List<Issue> listResolvedForStudent(EntityManager em, Long userID, String courseInstanceID);
	public List<Issue> listIssuesForActivation(EntityManager em, String courseInstanceID);
	public Long checkIssueUpdatesStaff(EntityManager em, String courseInstanceID,Long userID);
	public List<Long> listAssistantsWithVersions(EntityManager em, String courseInstanceID);
	public List<Issue> listIssuesOnCourse(EntityManager em, String courseInstanceID);
	public void saveIssueVersion(EntityManager em, IssueAssistantVersion iav);
}
