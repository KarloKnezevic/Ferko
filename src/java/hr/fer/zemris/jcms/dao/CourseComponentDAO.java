package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.AbstractCourseComponentDef;
import hr.fer.zemris.jcms.model.CCIAAssignment;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentDescriptor;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentItemAssessment;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseComponentTaskAssignment;
import hr.fer.zemris.jcms.model.CourseComponentTaskUpload;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.ItemDescriptionFile;
import hr.fer.zemris.jcms.model.TaskDescriptionFile;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.assessments.StudentTask;

import java.util.List;

import javax.persistence.EntityManager;

public interface CourseComponentDAO {
	public void save(EntityManager em, CourseComponentDescriptor descriptor);
	public void remove(EntityManager em, CourseComponentDescriptor descriptor);
	/**
	 * Vraca sve CourseComponentDescriptore koji postoje u sustavu
	 * @param em
	 * @return
	 */
	public List<CourseComponentDescriptor> listDescriptors(EntityManager em);
	/**
	 * Vraca courseComponentDescriptor sa zadanim shortName-om ili null ako takav ne postoji
	 * @param em
	 * @param shortName
	 * @return
	 */
	public CourseComponentDescriptor getByShortName(EntityManager em, String shortName);
	public void save(EntityManager em, CourseComponent component);
	public void remove(EntityManager em, CourseComponent component);
	public CourseComponent getCourseComponent(EntityManager em, Long id);
	/**
	 * Vraca listu CourseComponenti na zadanom CourseInstance.u
	 * @param em
	 * @param courseInstance
	 * @return
	 */
	public List<CourseComponent> listComponentsOnCourse(EntityManager em, CourseInstance courseInstance);
	public CourseComponentItem getItem(EntityManager em, Long id);
	public void save(EntityManager em, CourseComponentItem item);
	public void remove(EntityManager em, CourseComponentItem item);
	public CourseComponentTask getTask(EntityManager em, Long id);
	public void save(EntityManager em, CourseComponentTask task);
	public void remove(EntityManager em, CourseComponentTask task);
	/**
	 * Metoda koja pronalazi CourseComponentTask po title.u
	 * OPASKA: Ova metoda je loša u smislu da je dozvoljeno da vise itema ima task koji se zove isto!!! Title nije unique!
	 * @param em
	 * @param title
	 * @return
	 */
	public CourseComponentTask findByTitle(EntityManager em, String title);
	/**
	 * Metoda koja pronalazi CourseComponentTask po title-u, i to unutar jednog CourseComponentItem-a.
	 * @param em
	 * @param title
	 * @param courseComponentItem
	 * @return
	 */
	public CourseComponentTask findByTitleOnItem(EntityManager em, String title, CourseComponentItem courseComponentItem);
	/**
	 * Metoda koja vraca listu CourseComponentTaskAssignmenta nekog taska
	 * @param em
	 * @param courseComponentTask
	 * @return
	 */
	public List<CourseComponentTaskAssignment> getTaskUsers(EntityManager em, CourseComponentTask courseComponentTask);
	public void save(EntityManager em, CourseComponentTaskAssignment ccta);
	public void remove(EntityManager em, CourseComponentTaskAssignment ccta);
	/**
	 * Metoda koja vraca objekt CourseComponentTaskAssignment zadanog usera na zadanom tasku.
	 * Ako user nije na tasku vraca null
	 * @return
	 */
	public CourseComponentTaskAssignment getAssignmentOnTask(EntityManager em, CourseComponentTask task, User user);
	public CourseComponentTaskAssignment getTaskAssignment(EntityManager em, Long id);
	public void save(EntityManager em, CourseComponentTaskUpload cctu);
	public void remove(EntityManager em, CourseComponentTaskUpload cctu);
	public CourseComponentTaskUpload getTaskUpload(EntityManager em, Long id);
	/**
	 * Metoda koja vraca listu objekata CourseComponentTask, 
	 * ciji je task na zadanom itemu, za zadanog usera
	 * @param cci
	 * @param currentUser
	 */
	public List<CourseComponentTask> findUserTasksOnItem(EntityManager em, CourseComponentItem cci, User user);
	public CourseComponentItemAssessment getItemAssessment(EntityManager em, Long id);
	public void save(EntityManager em, CourseComponentItemAssessment ccia);
	public void remove(EntityManager em, CourseComponentItemAssessment ccia);
	public List<CCIAAssignment> getItemAssessmentUsers(EntityManager em,
			CourseComponentItemAssessment ccia);
	public void save(EntityManager em, CCIAAssignment cciaa);
	public void remove(EntityManager em, CCIAAssignment cciaa);
	/**
	 * Metoda koja vraca listu objekata CourseComponentItemAssessment, 
	 * ciji je assessment na zadanom itemu, za zadanog usera
	 * @param cci
	 * @param currentUser
	 */
	public List<CourseComponentItemAssessment> findUserAssessmentsOnItem(
			EntityManager em, CourseComponentItem cci, User currentUser);
	public CCIAAssignment findUserAssessmentAssignment(
			EntityManager em, CourseComponentItemAssessment ccia, User user);
	public void save(EntityManager em, ItemDescriptionFile idf);
	public void remove(EntityManager em, ItemDescriptionFile idf);
	public ItemDescriptionFile getItemDescriptionFile(EntityManager em, Long id);
	
	public void save(EntityManager em, TaskDescriptionFile tdf);
	public void remove(EntityManager em, TaskDescriptionFile tdf);
	public TaskDescriptionFile getTaskDescriptionFile(EntityManager em, Long id);
	public void save(EntityManager em, AbstractCourseComponentDef def);
	public void remove(EntityManager em, AbstractCourseComponentDef def);
	public AbstractCourseComponentDef getDef(EntityManager em, Long id);
	public List<CourseComponentTask> findTasksForReviewer(EntityManager em, CourseComponentItem item, User user);
	public List<CourseComponentTaskAssignment> listTaskAssignments(EntityManager em, CourseComponentTask cct);
	
	public CourseComponentItem findItem(EntityManager em, String courseInstanceID, String groupRoot, int position);
	public List<StudentTask> findStudentTasks(EntityManager em, String courseInstanceID, String componentShortName, int position);
	public List<CourseComponentTask> listTasksForItem(EntityManager em, CourseInstance courseInstance, String componentShortName, int position);
}
