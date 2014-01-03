package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.extra.ToDoTaskPriority;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * ToDo task model. 
 * 
 * @author IvanFer
 * 
 */

////Upiti za formiranje liste "Moji ToDo taskovi"
//findByRealizer2 - Dohvaća sve taskove za realizatora X koji (su otvoreni i nemaju roditelja) ili imaju otvorenog roditelja
//					To će dohvatiti samo taskove dodijeljene direktno/individualno, a ignorirat će taskove dobivene implicitno putem grupe.
//findGroupTasksByRealizer - Dohvaća sve taskove za realizatora X koji kao roditelja imaju grupni task

////Upiti za formiranje liste "ToDo taskovi zadani drugima"
//findByOwnerOnly - Dohvaća sve taskova za vlasnika X kojima realizator nije X i koji su (otvoreni i nemaju roditelja) ili imaju otvorenog roditelja
//					To će dohvatiti samo taskove dodijeljene direktno/individualno, a ignorirat će taskove dobivene implicitno putem grupe.
//findGroupTasksByOwner - Dohvaća sve grupne taskove za vlasnika X


@NamedQueries({
	@NamedQuery(name="ToDoTask.findSingleFamily",query="select tdt from ToDoTask as tdt where tdt.id=:taskId or tdt.parentTask.id=:taskId"),
	@NamedQuery(name="ToDoTask.findSingleTask",query="select tdt from ToDoTask as tdt where tdt.id=:taskId"),
	@NamedQuery(name="ToDoTask.findGroupTask", query="select tdt from ToDoTask as tdt where tdt.id=:taskId and tdt.status like 'GROUP_TASK' or tdt.parentTask.id=:taskId and tdt.status like 'GROUP_TASK'"),
    @NamedQuery(name="ToDoTask.findByOwnerOnly",query="select tdt from ToDoTask as tdt where tdt.owner.id=:ownId and tdt.realizer.id <> :ownId and ((tdt.status like 'OPEN' and tdt.parentTask is null) OR (tdt.parentTask is not null and (select tdd.status from ToDoTask as tdd where tdd.id=tdt.parentTask.id) like 'OPEN'))"),
    @NamedQuery(name="ToDoTask.findByRealizer",query="select tdt from ToDoTask as tdt where tdt.realizer.id=:reaId and tdt.status like 'OPEN'"),
    @NamedQuery(name="ToDoTask.findByRealizer2",query="select tdt from ToDoTask as tdt where tdt.realizer.id=:reaId and ((tdt.status like 'OPEN' and tdt.parentTask is null) OR (tdt.parentTask is not null and (select tdd.status from ToDoTask as tdd where tdd.id=tdt.parentTask.id) like 'OPEN'))"),
    @NamedQuery(name="ToDoTask.findGroupTasksByRealizer",query="select tdt from ToDoTask as tdt where tdt.realizer.id=:reaId and tdt.status not like 'GROUP_TASK%' and tdt.parentTask.status like 'GROUP_TASK%'"),
    @NamedQuery(name="ToDoTask.findChildrenByParentID",query="select tdt from ToDoTask as tdt where tdt.parentTask.id=:reaId"),
    @NamedQuery(name="ToDoTask.findGroupTasksByOwner",query="select tdt from ToDoTask as tdt where tdt.owner.id=:ownId and tdt.status like 'GROUP_TASK%'"),
    @NamedQuery(name="ToDoTask.findPublicTemplates",query="select tdt from ToDoTask as tdt where tdt.status like 'PUBLIC_TEMPLATE'"),
    @NamedQuery(name="ToDoTask.deleteTask",query="delete from ToDoTask as tdt where tdt.id=:taskId"),
    @NamedQuery(name="ToDoTask.findOwnByStatus",query="select tdt from ToDoTask as tdt where tdt.owner.id=:ownId and tdt.status like :status"),
    @NamedQuery(name="ToDoTask.getChildrenWithStatus",query="select tdt from ToDoTask as tdt where tdt.parentTask.id=:parentID and tdt.status like :status"),
    @NamedQuery(name="ToDoTask.getChildrenForRealizer",query="select tdt from ToDoTask as tdt where tdt.parentTask.id=:parentID and tdt.realizer.id = :reaID"),
    @NamedQuery(name="ToDoTask.findGroupTaskRealizers",query="select tdt.realizer from ToDoTask as tdt where tdt.parentTask.id=:taskId and tdt.status not like 'GROUP_TASK%'")
})
@Entity
@Table(name="todo_tasks")
public class ToDoTask implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private User owner;
	private User realizer;
	private ToDoTask parentTask;
	private ToDoTaskStatus status;
	private Date deadline;
	private String title;
	private String description;
	private Boolean garbageCollectable;
	private ToDoTaskPriority priority;
	private int version;
	
	/**
	 * Task ID
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Task owner - user who created the task
	 * @return Owner
	 */
	@JoinColumn(nullable = false)
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	/**
	 * Task realizer - user who must do the task
	 * @return Realizer
	 */
	@JoinColumn(nullable = false)
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getRealizer() {
		return realizer;
	}
	public void setRealizer(User realizer) {
		this.realizer = realizer;
	}
	
	/**
	 * Parent ToDo task
	 * @return Parent
	 */
	@JoinColumn(nullable = true)
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public ToDoTask getParentTask() {
		return parentTask;
	}
	public void setParentTask(ToDoTask parentTask) {
		this.parentTask = parentTask;
	}
	
	/**
	 * Task status (OPEN, CLOSED)
	 * @return Status
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public ToDoTaskStatus getStatus() {
		return status;
	}
	public void setStatus(ToDoTaskStatus status) {
		this.status = status;
	}
	
	/**
	 * Deadline for task completion
	 * @return Date
	 */
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	/**
	 * Detailed task description
	 * @return Description
	 */
	@Column(length = 1000, nullable = true)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Task title
	 * @return Title
	 */
	@Column(length = 100, nullable = false)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Indicator whether the task can be removed from the database
	 * during a regular cleanup.
	 * @return Indicator
	 */
	@Column(nullable = false)
	public Boolean getGarbageCollectable() {
		return garbageCollectable;
	}
	public void setGarbageCollectable(Boolean garbageCollectable) {
		this.garbageCollectable = garbageCollectable;
	}
	
	/**
	 * Task priority (TRIVIAL, MEDIUM, CRITICAL)
	 * @return Priority
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public ToDoTaskPriority getPriority() {
		return priority;
	}
	public void setPriority(ToDoTaskPriority priority) {
		this.priority = priority;
	}
	
	/**
	 * Version. Optimistic lock support.
	 * @return
	 */
	@Version
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOwner().hashCode();
		result = prime * result + getRealizer().hashCode();
		result = prime * result + getTitle().hashCode();
		result = prime * result 
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());
		result = prime * result
				+ getStatus().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ToDoTask))
			return false;
		final ToDoTask other = (ToDoTask) obj;
		if (other.getOwner().equals(this.getOwner()) && 
				other.getRealizer().equals(this.getRealizer()) &&
					other.getTitle().equals(this.getTitle()) &&
						other.getDescription().equals(this.getDescription()) &&
							other.getStatus().equals(this.getStatus()))
		{
			return true;
		}else{
			return false;	
		}
		
	}
	
}
