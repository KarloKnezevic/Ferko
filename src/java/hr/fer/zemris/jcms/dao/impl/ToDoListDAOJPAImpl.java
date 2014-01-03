package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.ToDoListDAO;
import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;

import java.util.List;

import javax.persistence.EntityManager;

public class ToDoListDAOJPAImpl implements ToDoListDAO {
 
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getTasksAssignedToOthers(EntityManager em, Long ownerID) {
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findByOwnerOnly").setParameter("ownId", ownerID).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getOwnTasks(EntityManager em, Long realizerID) {
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findByRealizer2").setParameter("reaId", realizerID).getResultList();
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public List<ToDoTask> getOwnTasksByStatus(EntityManager em, Long ownerID, ToDoTaskStatus status){
            return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findOwnByStatus").setParameter("ownId", ownerID).setParameter("status", status).getResultList();
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getOwnGroupTasks(EntityManager em, Long realizerID) {
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findGroupTasksByRealizer").setParameter("reaId", realizerID).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getChildrenForParentID(EntityManager em, Long parentID){
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findChildrenByParentID").setParameter("reaId", parentID).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getGroupTasksAssignedToOthers(EntityManager em, Long ownerID){
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findGroupTasksByOwner").setParameter("ownId", ownerID).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getPublicTemplates(EntityManager em){
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findPublicTemplates").getResultList();
	}
	
	@Override
	public ToDoTask getSingleTask(EntityManager em, Long id) {
		return (ToDoTask)em.createNamedQuery("ToDoTask.findSingleTask").setParameter("taskId", id).getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getTasksFamily(EntityManager em, Long id) {
		return em.createNamedQuery("ToDoTask.findSingleFamily").setParameter("taskId", id).getResultList();
	}


	@Override
	public void insertTask(EntityManager em, ToDoTask task) {
		em.persist(task);
	}
	
	@Override
	public void updateTask(EntityManager em, ToDoTask task) {
		em.merge(task);
	}

	@Override
	public ToDoTask refreshTask(EntityManager em, ToDoTask task){
		em.refresh(task);
		return task;
	}
	
	@Override
	public void closeTask(EntityManager em, Long taskId){
		ToDoTask task = em.find(ToDoTask.class, taskId);
		task.setStatus(ToDoTaskStatus.CLOSED);
		em.persist(task);
	}
	
	@Override
	public void deleteTask(EntityManager em, Long taskId){
		em.createNamedQuery("ToDoTask.deleteTask").setParameter("taskId", taskId).executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getChildrenWithStatus(EntityManager em, Long parentID, ToDoTaskStatus status){
		return em.createNamedQuery("ToDoTask.getChildrenWithStatus").setParameter("parentID", parentID).setParameter("status", status).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getChildrenForRealizer(EntityManager em, Long parentID, Long realizerID){
		return em.createNamedQuery("ToDoTask.getChildrenForRealizer").setParameter("parentID", parentID).setParameter("reaID", realizerID).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ToDoTask> getGroupTask(EntityManager em, Long taskID){
		return (List<ToDoTask>)em.createNamedQuery("ToDoTask.findGroupTask").setParameter("taskId", taskID).getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getGroupTaskRealizers(EntityManager em, Long taskID){
		return (List<User>)em.createNamedQuery("ToDoTask.findGroupTaskRealizers").setParameter("taskId", taskID).getResultList();
	}
}
