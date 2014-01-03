package hr.fer.zemris.jcms.dao;

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

public interface AssessmentDAO {
	public List<Assessment> listForCourseInstance(EntityManager em, String courseInstanceID);
	public Assessment get(EntityManager em, Long id);
	public void save(EntityManager em, Assessment assessment);
	public void remove(EntityManager em, Assessment assessment);
	public void save(EntityManager em, AssessmentConfiguration assessmentConfiguration);
	public void remove(EntityManager em, AssessmentConfiguration assessmentConfiguration);
	public void save(EntityManager em, AssessmentConfProblemsData assessmentConfProblemsData);
	public void remove(EntityManager em, AssessmentConfProblemsData assessmentConfProblemsData);
	public void save(EntityManager em, AssessmentConfChoiceAnswers assessmentConfChoiceAnswers);
	public void remove(EntityManager em, AssessmentConfChoiceAnswers assessmentConfChoiceAnswers);
	public void save(EntityManager em, AssessmentFlag assessmentFlag);
	public void remove(EntityManager em, AssessmentFlag assessmentFlag);
	public void save(EntityManager em, AssessmentAppealInstance assessmentAppealInstance);
	public void remove(EntityManager em, AssessmentAppealInstance assessmentAppealInstance);
	public List<AssessmentScore> listScoresForCourseInstance(EntityManager em, CourseInstance courseInstance);
	public AssessmentScore getScore(EntityManager em, Long id);
	public AssessmentScore getScore(EntityManager em, Assessment assessment, User user);
	public void save(EntityManager em, AssessmentScore assessmentScore);
	public void remove(EntityManager em, AssessmentScore assessmentScore);
	public List<AssessmentFlagValue> listFlagValuesForCourseInstance(EntityManager em, CourseInstance courseInstance);
	/**
	 * Vraća sve vrijednosti zastavica za korisnika na kolegiju.
	 * @param em
	 * @param courseInstance
	 * @param user
	 * @return
	 */
	public List<AssessmentFlagValue> listFlagValuesForCourseInstanceAndUser(EntityManager em, CourseInstance courseInstance, User user);
	public AssessmentFlagValue getFlagValue(EntityManager em, Long id);
	public AssessmentFlagValue getFlagValue(EntityManager em, AssessmentFlag flag, User user);
	public void save(EntityManager em, AssessmentFlagValue assessmentFlagValue);
	public void remove(EntityManager em, AssessmentFlagValue assessmentFlagValue);
	public List<AssessmentFlag> listFlagsForCourseInstance(EntityManager em, String courseInstanceID);
	public AssessmentFlag getFlag(EntityManager em, Long id);
	public List<Assessment> findTaggedOnSemester(EntityManager em, YearSemester yearSemester, AssessmentTag assessmentTag);
	/**
	 * Vraća popis oblika (isvuSifra,kratkoImeProvjere) za sve provjere na svim kolegijima koji su u trazenom semestru.
	 * @param em entity manager
	 * @param yearSemester semestar
	 * @return popis
	 */
	public List<Object[]> findShortNamesOnSemester(EntityManager em, YearSemester yearSemester);
	/**
	 * Pronalazi sve korisnike koji imaju dignutu zadanu zastavicu
	 * @param em
	 * @param flag
	 * @return
	 */
	public List<User> listUsersWithFlagUp(EntityManager em, AssessmentFlag flag);
	/**
	 * Pronalazi sve datoteke na provjeri (bilo da su za korisnika, bilo za provjeru).
	 * 
	 * @param em
	 * @param assessment
	 * @return
	 */
	public List<AssessmentFile> listAssessmentFiles(EntityManager em, Assessment assessment);
	/**
	 * Pronalazi sve datoteke na provjeri koje su za korisnike.
	 * 
	 * @param em
	 * @param assessment
	 * @return
	 */
	public List<AssessmentFile> listAssessmentFilesForUsers(EntityManager em, Assessment assessment);
	/**
	 * Pronalazi sve datoteke koje pripadaju samoj provjeri, a ne korisniku (dakle, imaju user=null).
	 * 
	 * @param em
	 * @param assessment
	 * @return
	 */
	public List<AssessmentFile> listAssessmentFilesForAssessment(EntityManager em, Assessment assessment);
	/**
	 * Pronalazi sve datoteke na provjeri za određenog korisnika.
	 * 
	 * @param em
	 * @param assessment
	 * @param user
	 * @return
	 */
	public List<AssessmentFile> listAssessmentFilesForUser(EntityManager em, Assessment assessment, User user);
	/**
	 * Pohranjuje datoteku u bazu.
	 * 
	 * @param em
	 * @param file
	 */
	public void save(EntityManager em, AssessmentFile file);
	/**
	 * Briše datoteku iz baze.
	 *  
	 * @param em
	 * @param file
	 */
	public void remove(EntityManager em, AssessmentFile file);
	/**
	 * Vraća sve bodove za sve provjere studenta na kolegiju.
	 * @param em
	 * @param courseInstance
	 * @param user
	 * @return
	 */
	public List<AssessmentScore> listScoresForCourseInstanceAndUser(EntityManager em, CourseInstance courseInstance, User user);
	public AssessmentRoom getAssessmentRoom(EntityManager em, Long id);
	public void save(EntityManager em, AssessmentAssistantSchedule aas);
	public void remove(EntityManager em, AssessmentAssistantSchedule aas);
	public List<AssessmentScore> listScoresForAssessment(EntityManager em, Assessment assessment);
	public List<AssessmentFlagValue> listFlagValuesForAssessmentFlag(EntityManager em, AssessmentFlag assessmentFlag);
	public AssessmentFile getAssessmentFile(EntityManager em, Long id);
	/**
	 * Vraća sve {@link AssessmentConfProblemsData} za pripadni {@link AssessmentConfProblems}
	 * @param em
	 * @param assessmentConfProblems
	 * @return
	 */
	public List<AssessmentConfProblemsData> listConfProblemsDataForAssessement(EntityManager em, AssessmentConfProblems assessmentConfProblems);
	/**
	 * Vraća {@link AssessmentConfProblemsData} za pripadni {@link AssessmentConfProblems} i ID usera.
	 * @param em
	 * @param assessmentConfProblems
	 * @param id
	 * @return
	 */
	public AssessmentConfProblemsData getConfProblemsDataForAssessementAndUserId(EntityManager em, AssessmentConfProblems assessmentConfProblems, Long id);
	/**
	 * Vraća sve {@link AssessmentAppealInstance} za pripadni {@link Assessment}
	 * @param em
	 * @param assessment
	 * @return
	 */
	public List<AssessmentAppealInstance> listAppealsForAssessment(EntityManager em, Assessment assessment);
	/**
	 * Vraća sve {@link AssessmentAppealInstance} za pripadni {@link CourseInstance}
	 * @param em
	 * @param course
	 * @return
	 */
	public List<AssessmentAppealInstance> listAppealsForCourse(EntityManager em, CourseInstance course);
	/**
	 * Vraća sve {@link AssessmentAppealInstance} za korisnika koji je stvorio žalbu i provjeru znanja kojoj žalba pripada.
	 * @param em
	 * @param assessment
	 * @param user
	 * @return
	 */
	public List<AssessmentAppealInstance> listAppealsForUserAndAssessment(EntityManager em, Assessment assessment, User user);
	/**
	 * Vraća {@link AssessmentAppealInstance} za pripadni id AssessmentAppealInstance-a
	 * @param em
	 * @param id
	 * @return
	 */
	public AssessmentAppealInstance getAppealForId(EntityManager em, Long id);
	
	/**
	 * Vraća {@link AssessmentConfChoiceAnswers} na provjeri <code>assessmentConfChoice</code>.
	 * @param em
	 * @param assessmentConfChoice
	 * @return
	 */
	public List<AssessmentConfChoiceAnswers> listAssessmentConfChoiceAnswersForAssessement(EntityManager em, AssessmentConfChoice assessmentConfChoice);
	
	/**
	 * Vraća {@link AssessmentConfChoiceAnswers} na određenoj provjeri za pripadnu grupu za predavanja.
	 * @param em
	 * @param group Grupa za predavanja.
	 * @param assessmentConfChoice
	 * @return
	 */
	public List<AssessmentConfChoiceAnswers> listAssessmentConfChoiceAnswersForAssessementAndGroup(EntityManager em, String group, AssessmentConfChoice assessmentConfChoice);
	
	/**
	 * Vraća {@link AssessmentConfChoiceAnswers} na određenoj provjeri za zadanog studenta.
	 * @param em
	 * @param user
	 * @param assessmentConfChoice
	 * @return
	 */
	public AssessmentConfChoiceAnswers getAssessmentConfChoiceAnswersForAssessementAndStudent(EntityManager em, User user, AssessmentConfChoice assessmentConfChoice);

	/**
	 * Pronalazi provjeru koja ima definiranog predanog ulancanog roditelja.
	 * @param chainedParent
	 * @return
	 */
	public Assessment findForChainedParent(EntityManager em, Assessment chainedParent);
}
