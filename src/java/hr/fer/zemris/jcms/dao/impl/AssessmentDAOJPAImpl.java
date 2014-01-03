package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.AssessmentDAO;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;

import java.util.List;

import javax.persistence.EntityManager;

public class AssessmentDAOJPAImpl implements AssessmentDAO {

	@Override
	public Assessment get(EntityManager em, Long id) {
		return em.find(Assessment.class, id);
	}

	@Override
	public AssessmentFlag getFlag(EntityManager em, Long id) {
		return em.find(AssessmentFlag.class, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Assessment> listForCourseInstance(EntityManager em, String courseInstanceID) {
		return (List<Assessment>)em.createNamedQuery("Assessment.listForCourseInstance").setParameter("courseInstanceID", courseInstanceID).getResultList();
	}

	@Override
	public void remove(EntityManager em, Assessment assessment) {
		em.remove(assessment);
	}

	@Override
	public void save(EntityManager em, Assessment assessment) {
		em.persist(assessment);
	}

	@Override
	public void remove(EntityManager em, AssessmentFlag assessmentFlag) {
		em.remove(assessmentFlag);
	}

	@Override
	public void save(EntityManager em, AssessmentFlag assessmentFlag) {
		em.persist(assessmentFlag);
	}

	@Override
	public AssessmentFlagValue getFlagValue(EntityManager em, Long id) {
		return em.find(AssessmentFlagValue.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AssessmentFlagValue getFlagValue(EntityManager em,
			AssessmentFlag flag, User user) {
		List<AssessmentFlagValue> l = (List<AssessmentFlagValue>)em.createNamedQuery("AssessmentFlagValue.getForAssessmentFlagAndUser")
		.setParameter("assessmentFlag", flag)
		.setParameter("user", user)
		.getResultList();
	if(l==null || l.isEmpty()) return null;
	return l.get(0);
	}
	
	@Override
	public AssessmentScore getScore(EntityManager em, Long id) {
		return em.find(AssessmentScore.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AssessmentScore getScore(EntityManager em, Assessment assessment,
			User user) {
		List<AssessmentScore> l = (List<AssessmentScore>)em.createNamedQuery("AssessmentScore.getForAssessmentAndUser")
			.setParameter("assessment", assessment)
			.setParameter("user", user)
			.getResultList();
		if(l==null || l.isEmpty()) return null;
		return l.get(0);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFlagValue> listFlagValuesForCourseInstance(
			EntityManager em, CourseInstance courseInstance) {
		return (List<AssessmentFlagValue>)em.createNamedQuery("AssessmentFlagValue.listForCourseInstance").setParameter("courseInstanceID", courseInstance.getId()).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFlagValue> listFlagValuesForCourseInstanceAndUser(
			EntityManager em, CourseInstance courseInstance, User user) {
		return (List<AssessmentFlagValue>)em.createNamedQuery("AssessmentFlagValue.listForCourseInstanceAndUser")
			.setParameter("courseInstance", courseInstance)
			.setParameter("user", user)
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentScore> listScoresForCourseInstance(EntityManager em,
			CourseInstance courseInstance) {
		return (List<AssessmentScore>)em.createNamedQuery("AssessmentScore.listForCourseInstance").setParameter("courseInstanceID", courseInstance.getId()).getResultList();
	}

	@Override
	public void remove(EntityManager em, AssessmentScore assessmentScore) {
		em.remove(assessmentScore);
	}

	@Override
	public void remove(EntityManager em, AssessmentFlagValue assessmentFlagValue) {
		em.remove(assessmentFlagValue);
	}

	@Override
	public void save(EntityManager em, AssessmentScore assessmentScore) {
		em.persist(assessmentScore);
	}

	@Override
	public void save(EntityManager em, AssessmentFlagValue assessmentFlagValue) {
		em.persist(assessmentFlagValue);
	}
	
	@Override
	public void remove(EntityManager em, AssessmentConfProblemsData assessmentConfProblemsData) {
		em.remove(assessmentConfProblemsData);
	}

	@Override
	public void save(EntityManager em, AssessmentConfProblemsData assessmentConfProblemsData) {
		em.persist(assessmentConfProblemsData);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFlag> listFlagsForCourseInstance(EntityManager em,
			String courseInstanceID) {
		return (List<AssessmentFlag>)em.createNamedQuery("AssessmentFlag.listForCourseInstance").setParameter("courseInstanceID", courseInstanceID).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Assessment> findTaggedOnSemester(EntityManager em,
			YearSemester yearSemester, AssessmentTag assessmentTag) {
		return (List<Assessment>)em.createNamedQuery("Assessment.findTaggedOnSemester")
			.setParameter("yearSemester", yearSemester)
			.setParameter("assessmentTag", assessmentTag)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findShortNamesOnSemester(EntityManager em,
			YearSemester yearSemester) {
		return (List<Object[]>)em.createNamedQuery("Assessment.findShortNamesOnSemester")
		.setParameter("yearSemester", yearSemester)
		.getResultList();
	}
	
	@Override
	public void remove(EntityManager em,
			AssessmentConfiguration assessmentConfiguration) {
		em.remove(assessmentConfiguration);
	}
	
	@Override
	public void save(EntityManager em,
			AssessmentConfiguration assessmentConfiguration) {
		em.persist(assessmentConfiguration);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFile> listAssessmentFiles(EntityManager em, Assessment assessment) {
		return (List<AssessmentFile>)em.createNamedQuery("AssessmentFile.listReallyAllForAssessment")
		.setParameter("assessment", assessment)
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFile> listAssessmentFilesForAssessment(
			EntityManager em, Assessment assessment) {
		return (List<AssessmentFile>)em.createNamedQuery("AssessmentFile.listAllForAssessmentOnly")
		.setParameter("assessment", assessment)
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFile> listAssessmentFilesForUser(
			EntityManager em, Assessment assessment, User user) {
		return (List<AssessmentFile>)em.createNamedQuery("AssessmentFile.listAllForAssessmentUser")
		.setParameter("assessment", assessment)
		.setParameter("user", user)
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFile> listAssessmentFilesForUsers(
			EntityManager em, Assessment assessment) {
		return (List<AssessmentFile>)em.createNamedQuery("AssessmentFile.listAllForAssessment")
			.setParameter("assessment", assessment)
			.getResultList();
	}
	
	@Override
	public void remove(EntityManager em, AssessmentFile file) {
		em.remove(file);
	}
	
	@Override
	public void save(EntityManager em, AssessmentFile file) {
		em.persist(file);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentScore> listScoresForCourseInstanceAndUser(
			EntityManager em, CourseInstance courseInstance, User user) {
		return (List<AssessmentScore>)em.createNamedQuery("AssessmentScore.listForCourseInstanceAndUser")
			.setParameter("courseInstance", courseInstance)
			.setParameter("user", user)
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersWithFlagUp(EntityManager em,
			AssessmentFlag flag) {
		return (List<User>)em.createNamedQuery("AssessmentFlag.listUsersWithFlagUp")
			.setParameter("assessmentFlag", flag)
			.getResultList(); 
	}
	
	@Override
	public AssessmentRoom getAssessmentRoom(EntityManager em, Long id) {
		return em.find(AssessmentRoom.class, id);
	}
	
	@Override
	public void save(EntityManager em, AssessmentAssistantSchedule aas) {
		em.persist(aas);
	}
	@Override
	public void remove(EntityManager em, AssessmentAssistantSchedule aas) {
		em.remove(aas);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentFlagValue> listFlagValuesForAssessmentFlag(
			EntityManager em, AssessmentFlag assessmentFlag) {
		return (List<AssessmentFlagValue>)em.createNamedQuery("AssessmentFlagValue.listForAssessmentFlag").setParameter("assessmentFlag", assessmentFlag).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentScore> listScoresForAssessment(EntityManager em,
			Assessment assessment) {
		return (List<AssessmentScore>)em.createNamedQuery("AssessmentScore.listForAssessment").setParameter("assessment", assessment).getResultList();
	}
	
	@Override
	public AssessmentFile getAssessmentFile(EntityManager em, Long id) {
		return em.find(AssessmentFile.class, id);
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#listConfProblemsDataForAssessement(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.AssessmentConfProblems)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentConfProblemsData> listConfProblemsDataForAssessement(EntityManager em, AssessmentConfProblems assessmentConfProblems) {
		return (List<AssessmentConfProblemsData>) em.createNamedQuery("AssessmentConfProblemsData.listConfProblemsDataForAssessement").setParameter("assessmentConfProblems", assessmentConfProblems).getResultList();
	}
	
	@Override
	public AssessmentConfProblemsData getConfProblemsDataForAssessementAndUserId(
			EntityManager em, AssessmentConfProblems assessmentConfProblems,
			Long id) {
		return (AssessmentConfProblemsData) em.createNamedQuery("AssessmentConfProblemsData.getConfProblemsDataForAssessementAndUserId").setParameter("assessmentConfProblems", assessmentConfProblems).setParameter("id", id).getSingleResult();
	}
	
	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#listAppealsForAssessment(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.Assessment)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentAppealInstance> listAppealsForAssessment(EntityManager em, Assessment assessment) {
		return (List<AssessmentAppealInstance>) em.createNamedQuery("AssessmentAppealInstance.listAppealsForAssessment").setParameter("assessment", assessment).getResultList();
	}
	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#listAppealsForAssessment(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.Assessment)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentAppealInstance> listAppealsForCourse(EntityManager em, CourseInstance course) {
		return (List<AssessmentAppealInstance>) em.createNamedQuery("AssessmentAppealInstance.listAppealsForCourse").setParameter("course", course).getResultList();
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#remove(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance)
	 */
	@Override
	public void remove(EntityManager em, AssessmentAppealInstance assessmentAppealInstance) {
		em.remove(assessmentAppealInstance);
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#save(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance)
	 */
	@Override
	public void save(EntityManager em, AssessmentAppealInstance assessmentAppealInstance) {
		em.persist(assessmentAppealInstance);
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#getAppealForId(javax.persistence.EntityManager, java.lang.Long)
	 */
	@Override
	public AssessmentAppealInstance getAppealForId(EntityManager em, Long id) {
		return (AssessmentAppealInstance) em.createNamedQuery("AssessmentAppealInstance.getAppealForId").setParameter("id", id).getSingleResult();
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#getAssessmentConfChoiceAnswersForAssessementAndStudent(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.User, hr.fer.zemris.jcms.model.AssessmentConfChoice)
	 */
	@Override
	public AssessmentConfChoiceAnswers getAssessmentConfChoiceAnswersForAssessementAndStudent(EntityManager em, User user, AssessmentConfChoice assessmentConfChoice) {
		return (AssessmentConfChoiceAnswers) em.createNamedQuery("AssessmentConfChoiceAnswers.getAssessmentConfChoiceAnswersForAssessementAndStudent").setParameter("assessmentConfChoice", assessmentConfChoice).setParameter("user", user).getSingleResult();
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#listAssessmentConfChoiceAnswersForAssessement(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.AssessmentConfChoice)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentConfChoiceAnswers> listAssessmentConfChoiceAnswersForAssessement(EntityManager em, AssessmentConfChoice assessmentConfChoice) {
		return (List<AssessmentConfChoiceAnswers>) em.createNamedQuery("AssessmentConfChoiceAnswers.listAssessmentConfChoiceAnswersForAssessement").setParameter("assessmentConfChoice", assessmentConfChoice).getResultList();
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#listAssessmentConfChoiceAnswersForStudentsGroup(javax.persistence.EntityManager, java.lang.String, hr.fer.zemris.jcms.model.AssessmentConfChoice)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentConfChoiceAnswers> listAssessmentConfChoiceAnswersForAssessementAndGroup(EntityManager em, String group, AssessmentConfChoice assessmentConfChoice) {
		return (List<AssessmentConfChoiceAnswers>) em.createNamedQuery("AssessmentConfChoiceAnswers.listAssessmentConfChoiceAnswersForAssessement").setParameter("assessmentConfChoice", assessmentConfChoice).setParameter("group", group).getResultList();
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#remove(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers)
	 */
	@Override
	public void remove(EntityManager em, AssessmentConfChoiceAnswers assessmentConfChoiceAnswers) {
		em.remove(assessmentConfChoiceAnswers);
		
	}

	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.dao.AssessmentDAO#save(javax.persistence.EntityManager, hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers)
	 */
	@Override
	public void save(EntityManager em, AssessmentConfChoiceAnswers assessmentConfChoiceAnswers) {
		em.persist(assessmentConfChoiceAnswers);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AssessmentAppealInstance> listAppealsForUserAndAssessment(
			EntityManager em, Assessment assessment, User user) {
		return (List<AssessmentAppealInstance>) em.createNamedQuery("AssessmentAppealInstance.listAppealsForUserAndAssessment").setParameter("assessment", assessment).setParameter("user", user).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Assessment findForChainedParent(EntityManager em, Assessment chainedParent) {
		List<Assessment> list = (List<Assessment>)em.createNamedQuery("Assessment.findForChainedParent").setParameter("chainedParent", chainedParent).getResultList();
		return list==null || list.isEmpty() ? null : list.get(0);
	}
}
