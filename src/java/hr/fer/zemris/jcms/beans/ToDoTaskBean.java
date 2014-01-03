package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.model.extra.ToDoTaskPriority;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;

import java.util.Date;

/**
 * ToDo task bean (used for initialization of todo tasks and testing)
 * @author IvanFer
 */
public class ToDoTaskBean {

	private String virtualID;
	private String ownerUserName;
	private String realizerUserName;
	private String parentTask;
	private ToDoTaskStatus status;
	private Date deadline;
	private String title;
	private String description;
	private Boolean garbageCollectable;
	private ToDoTaskPriority priority;
	
	
	public String getVirtualID() {
		return virtualID;
	}
	public void setVirtualID(String virtualID) {
		this.virtualID = virtualID;
	}
	
	public String getParentTask() {
		return parentTask;
	}
	public void setParentTask(String parentTask) {
		this.parentTask = parentTask;
	}
	
	
	public String getOwnerUserName() {
		return ownerUserName;
	}
	public void setOwnerUserName(String ownerUserName) {
		this.ownerUserName = ownerUserName;
	}
	public String getRealizerUserName() {
		return realizerUserName;
	}
	public void setRealizerUserName(String realizerUserName) {
		this.realizerUserName = realizerUserName;
	}
	public ToDoTaskStatus getStatus() {
		return status;
	}
	public void setStatus(ToDoTaskStatus status) {
		this.status = status;
	}
	
	
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public ToDoTaskPriority getPriority() {
		return priority;
	}
	public void setPriority(ToDoTaskPriority priority) {
		this.priority = priority;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
	public Boolean getGarbageCollectable() {
		return garbageCollectable;
	}
	public void setGarbageCollectable(Boolean garbageCollectable) {
		this.garbageCollectable = garbageCollectable;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToDoTaskBean other = (ToDoTaskBean) obj;
		if (other.getOwnerUserName().equals(this.getOwnerUserName()) && 
				other.getRealizerUserName().equals(this.getRealizerUserName()) &&
					other.getDeadline().equals(this.getDeadline()) &&
						other.getDescription().equals(this.getDescription()))
		{
			return true;
		}else{
			return false;	
		}
		
	}
	
	public String toString(){
		return "Owner: " + this.getOwnerUserName() 
			 + "\nRealizer: " + this.getRealizerUserName()
			 + "\nParent: " + this.getParentTask()
			 + "\nStatus: " + this.getStatus()
			 + "\nDeadline: " + this.getDeadline().toString()
			 + "\nTitle: " + this.getTitle()
			 + "\nDescription: " + this.getDescription()
			 + "\nPriority: " + this.getPriority()
			 + "\nGarbage collectable: " + this.getGarbageCollectable()
			 + "\n\n";
	}

	
}
