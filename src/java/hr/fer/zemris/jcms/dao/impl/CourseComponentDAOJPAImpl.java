package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.CourseComponentDAO;
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

public class CourseComponentDAOJPAImpl implements CourseComponentDAO{

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentDescriptor> listDescriptors(EntityManager em) {
		return (List<CourseComponentDescriptor>)em.createNamedQuery("CourseComponentDescriptor.list")
				.getResultList();
	}

	@Override
	public void remove(EntityManager em, CourseComponentDescriptor descriptor) {
		em.remove(descriptor);
	}

	@Override
	public void save(EntityManager em, CourseComponentDescriptor descriptor) {
		em.persist(descriptor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CourseComponentDescriptor getByShortName(EntityManager em,String shortName) {
		List<CourseComponentDescriptor> list = 
			(List<CourseComponentDescriptor>)em.createNamedQuery("CourseComponentDescriptor.getByShortName")
			.setParameter("shortName", shortName).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponent> listComponentsOnCourse(EntityManager em,
			CourseInstance courseInstance) {
		return (List<CourseComponent>)em.createNamedQuery("CourseComponent.listOnCourse")
			.setParameter("courseInstance", courseInstance)
			.getResultList();
	}

	@Override
	public void remove(EntityManager em, CourseComponent component) {
		em.remove(component);
	}

	@Override
	public void save(EntityManager em, CourseComponent component) {
		em.persist(component);
	}
	
	@Override
	public CourseComponent getCourseComponent(EntityManager em, Long id) {
		return em.find(CourseComponent.class, id);
	}

	@Override
	public void remove(EntityManager em, CourseComponentItem item) {
		em.remove(item);
	}

	@Override
	public void save(EntityManager em, CourseComponentItem item) {
		em.persist(item);
	}
	
	@Override
	public CourseComponentItem getItem(EntityManager em, Long id) {
		return em.find(CourseComponentItem.class,id);	
	}

	@Override
	public CourseComponentTask getTask(EntityManager em, Long id) {
		return em.find(CourseComponentTask.class, id);
	}

	@Override
	public void remove(EntityManager em, CourseComponentTask task) {
		em.remove(task);
	}

	@Override
	public void save(EntityManager em, CourseComponentTask task) {
		em.persist(task);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CourseComponentTask findByTitle(EntityManager em, String title) {
		List<CourseComponentTask> list = 
			(List<CourseComponentTask>)em.createNamedQuery("CourseComponentTask.findByTitle")
			.setParameter("title", title)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CourseComponentTask findByTitleOnItem(EntityManager em, String title, CourseComponentItem courseComponentItem) {
		List<CourseComponentTask> list = 
			(List<CourseComponentTask>)em.createNamedQuery("CourseComponentTask.findByTitleOnItem")
			.setParameter("title", title)
			.setParameter("courseComponentItem", courseComponentItem)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentTaskAssignment> getTaskUsers(EntityManager em,
			CourseComponentTask courseComponentTask) {
		return (List<CourseComponentTaskAssignment>)em.createNamedQuery("CourseComponentTask.getTaskUsers")
			.setParameter("courseComponentTask", courseComponentTask)
			.getResultList();
	}
	
	@Override
	public void save(EntityManager em, CourseComponentTaskAssignment ccta) {
		em.persist(ccta);
	}
	
	@Override
	public void remove(EntityManager em, CourseComponentTaskAssignment ccta) {
		em.remove(ccta);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CourseComponentTaskAssignment getAssignmentOnTask(EntityManager em,
			CourseComponentTask task, User user) {
		List<CourseComponentTaskAssignment> list = 
			(List<CourseComponentTaskAssignment>)em.createNamedQuery("CourseComponentTask.getAssignmentOnTask")
			.setParameter("courseComponentTask", task)
			.setParameter("user", user)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@Override
	public CourseComponentTaskAssignment getTaskAssignment(EntityManager em,
			Long id) {
		return em.find(CourseComponentTaskAssignment.class, id);
	}

	@Override
	public CourseComponentTaskUpload getTaskUpload(EntityManager em, Long id) {
		return em.find(CourseComponentTaskUpload.class, id);		
	}

	@Override
	public void remove(EntityManager em, CourseComponentTaskUpload cctu) {
		em.remove(cctu);
	}

	@Override
	public void save(EntityManager em, CourseComponentTaskUpload cctu) {
		em.persist(cctu);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentTask> findUserTasksOnItem(EntityManager em, 
			CourseComponentItem cci, User user) {
		return (List<CourseComponentTask>)em.createNamedQuery("CourseComponentTask.listUserTasks")
			.setParameter("courseComponentItem", cci)
			.setParameter("user", user)
			.getResultList();
			
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentTask> listTasksForItem(EntityManager em, CourseInstance courseInstance, String componentShortName, int position) {
		return (List<CourseComponentTask>)em.createNamedQuery("CourseComponentTask.listTasksOnItem")
			.setParameter("courseInstance", courseInstance)
			.setParameter("position", position)
			.setParameter("componentShortName", componentShortName)
			.getResultList();
			
	}

	@Override
	public CourseComponentItemAssessment getItemAssessment(EntityManager em,
			Long id) {
		return em.find(CourseComponentItemAssessment.class, id);
	}

	@Override
	public void remove(EntityManager em, CourseComponentItemAssessment ccia) {
		em.remove(ccia);
	}

	@Override
	public void save(EntityManager em, CourseComponentItemAssessment ccia) {
		em.persist(ccia);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CCIAAssignment> getItemAssessmentUsers(EntityManager em,
			CourseComponentItemAssessment ccia) {
		return (List<CCIAAssignment>)em.createNamedQuery("CourseComponentItemAssessment.getItemAssessmentUsers")
		.setParameter("courseComponentItemAssessment", ccia)
		.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public CCIAAssignment findUserAssessmentAssignment(EntityManager em,
			CourseComponentItemAssessment ccia, User user) {
		List<CCIAAssignment> list = (List<CCIAAssignment>)em.createNamedQuery("CourseComponentItemAssessment.getItemAssessmentAssignment")
		.setParameter("courseComponentItemAssessment", ccia)
		.setParameter("user", user)
		.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@Override
	public void remove(EntityManager em, CCIAAssignment cciaa) {
		em.remove(cciaa);
	}

	@Override
	public void save(EntityManager em, CCIAAssignment cciaa) {
		em.persist(cciaa);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentItemAssessment> findUserAssessmentsOnItem(
			EntityManager em, CourseComponentItem cci, User user) {
		return (List<CourseComponentItemAssessment>)em.createNamedQuery("CourseComponentItemAssessment.listUserAssessments")
			.setParameter("courseComponentItem", cci)
			.setParameter("user", user)
			.getResultList();

	}

	@Override
	public ItemDescriptionFile getItemDescriptionFile(EntityManager em, Long id) {
		return em.find(ItemDescriptionFile.class, id);
	}

	@Override
	public TaskDescriptionFile getTaskDescriptionFile(EntityManager em, Long id) {
		return em.find(TaskDescriptionFile.class, id);
	}

	@Override
	public void remove(EntityManager em, ItemDescriptionFile idf) {
		em.remove(idf);
	}

	@Override
	public void remove(EntityManager em, TaskDescriptionFile tdf) {
		em.remove(tdf);
	}

	@Override
	public void save(EntityManager em, ItemDescriptionFile idf) {
		em.persist(idf);
	}

	@Override
	public void save(EntityManager em, TaskDescriptionFile tdf) {
		em.persist(tdf);
	}

	@Override
	public void remove(EntityManager em, AbstractCourseComponentDef def) {
		em.remove(def);
	}

	@Override
	public void save(EntityManager em, AbstractCourseComponentDef def) {
		em.persist(def);
	}

	@Override
	public AbstractCourseComponentDef getDef(EntityManager em, Long id) {
		return em.find(AbstractCourseComponentDef.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentTask> findTasksForReviewer(EntityManager em,
			CourseComponentItem item, User user) {
		return (List<CourseComponentTask>)em.createNamedQuery("CourseComponentTask.listReviewersTask")
			.setParameter("user", user)
			.setParameter("item", item)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CourseComponentTaskAssignment> listTaskAssignments(
			EntityManager em, CourseComponentTask cct) {
		return (List<CourseComponentTaskAssignment>)em.createNamedQuery("CourseComponentTask.listAssignments")
			.setParameter("courseComponentTask", cct)
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CourseComponentItem findItem(EntityManager em, String courseInstanceID, String groupRoot, int position) {
		List<CourseComponentItem> list = 
			(List<CourseComponentItem>)em.createNamedQuery("CourseComponentItem.findForCDP")
			.setParameter("courseInstanceID", courseInstanceID)
			.setParameter("groupRoot", groupRoot)
			.setParameter("position", position)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StudentTask> findStudentTasks(EntityManager em, String courseInstanceID, String componentShortName, int position) {
		List<StudentTask> list = (List<StudentTask>)em.createNamedQuery("CourseComponentTaskAssignment.findForItem")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("shortName", componentShortName)
		.setParameter("position", position)
		.getResultList(); 
		return list;
	}
}
