package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;

import java.util.List;

import javax.persistence.EntityManager;

public interface ToDoListDAO {

	public List<ToDoTask> getTasksAssignedToOthers(EntityManager em, Long ownerID);
	public List<ToDoTask> getOwnTasks(EntityManager em, Long ownerID);
	public List<ToDoTask> getOwnGroupTasks(EntityManager em, Long realizerID);
	public ToDoTask getSingleTask(EntityManager em, Long taskId);
	public List<ToDoTask> getTasksFamily(EntityManager em, Long taskID);
	public void insertTask(EntityManager em, ToDoTask task);
	public void updateTask(EntityManager em, ToDoTask task);
	public void closeTask(EntityManager em, Long taskId);
	public void deleteTask(EntityManager em, Long taskId);
	public ToDoTask refreshTask(EntityManager em, ToDoTask task);
	public List<ToDoTask> getChildrenForParentID(EntityManager em, Long parentID);
	public List<ToDoTask> getGroupTasksAssignedToOthers(EntityManager em, Long ownerID);
	public List<ToDoTask> getPublicTemplates(EntityManager em);
	public List<ToDoTask> getChildrenWithStatus(EntityManager em, Long parentID, ToDoTaskStatus status);
	public List<ToDoTask> getChildrenForRealizer(EntityManager em, Long parentID, Long realizerID);
	public List<ToDoTask> getGroupTask(EntityManager em, Long taskID);
	public List<ToDoTask> getOwnTasksByStatus(EntityManager em, Long userID, ToDoTaskStatus status);
	public List<User> getGroupTaskRealizers(EntityManager em, Long taskID);
}
