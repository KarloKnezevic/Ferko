package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ToDoTaskPriority;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;
import hr.fer.zemris.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ToDo task bean
 * @author IvanFer
 */
public class ToDoBean implements Comparable<ToDoBean>{

	private Long id;
	private User owner;
	private User realizer;
	private List<ToDoBean> subTasks;
	private ToDoTaskStatus status;
	private Date deadline;
	private String title;
	private String description;
	private Boolean garbageCollectable;
	private ToDoTaskPriority priority;
	private int version;
	private SimpleDateFormat sdf;
	private String percentClosed;
	private Boolean canEdit = false;
	
	public ToDoBean(){
		try{
			sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			this.setDeadline(sdf.parse(sdf.format(new Date())));
		}catch(ParseException pe){
			pe.printStackTrace();
		}
		this.setRealizer(new User());
		this.setOwner(new User());
	}
	
	public ToDoBean(ToDoTask task){
		try{
			sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			this.setId(task.getId());
			this.setDeadline(sdf.parse(sdf.format(task.getDeadline())));
			this.setDescription(task.getDescription());
			this.setGarbageCollectable(task.getGarbageCollectable());
			this.setOwner(task.getOwner());
			this.setPriority(task.getPriority());
			this.setRealizer(task.getRealizer());
			this.setStatus(task.getStatus());
			this.setTitle(task.getTitle());
			this.setVersion(task.getVersion());
		}catch(ParseException ignored){}
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long taskId) {
		this.id = taskId;
	}
	
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public String getOwnerFullName(){
		return this.getOwner().getFirstName() + " " + this.getOwner().getLastName();
	}
	
	public User getRealizer() {
		return realizer;
	}
	public void setRealizer(User realizer) {
		this.realizer = realizer;
	}
	
	public String getRealizerFullName(){
		return this.getRealizer().getFirstName() + " " 
				+ this.getRealizer().getLastName()
				+ " / " + this.getRealizer().getJmbag();
	}
	
	
	public List<ToDoBean> getSubTasks() {
		return subTasks;
	}
	public void setSubTasks(List<ToDoBean> subs) {
		this.subTasks = subs;
	}


	
	public ToDoTaskStatus getStatus() {
		return status;
	}
	public void setStatus(ToDoTaskStatus status) {
		this.status = status;
	}
	
	public String getStatusString(){
		if (this.status!=null) return status.name();
		else return null;
	}
	
	public void setStatusString(String status){
		if (!StringUtil.isStringBlank(status)) this.status = ToDoTaskStatus.valueOf(status);
	}
	
	public Date getDeadline() {
		return deadline;
	}
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	public void setDeadlineString(String deadlineString){
		try{
			String[] tmp1 = deadlineString.split(" ");
			String[] hoursMinutes = tmp1[1].split(":");
			Integer hours = Integer.parseInt(hoursMinutes[0]);
			if (hours<0 || hours > 23) return;
			Integer minutes = Integer.parseInt(hoursMinutes[1]);
			if (minutes<0 || minutes > 59) return;
			this.setDeadline(sdf.parse(deadlineString));
		}catch(ParseException ignored){	}
	}
	public String getDeadlineString(){
		return this.sdf.format(getDeadline());
	}
	
	public String getGroupTaskDescription(){
		String[] tmp = this.description.split("#_#");
		if (tmp.length>1) return tmp[1];
		else return "";
	}
	
	public void setGroupTaskDescription(String groupTaskDescription){
		String[] tmp = this.description.split("#_#");
		if (tmp.length>0)
			this.setDescription(tmp[0]+"#_#"+groupTaskDescription);
	}
	
	public Boolean getMasterGroupTask(){
		if(this.description.contains("#_#")) return true;
		else return false;
	}
	
	public String getDescription() {
		return this.description;	
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
	
	public String getPriorityString() {
		return priority.name();
	}

	public void setPriorityString(String priorityString) {
		this.priority = ToDoTaskPriority.valueOf(priorityString);
	}

	public int getSubTaskQuantity(){
		if(this.subTasks==null) return 0;
		else return this.subTasks.size();
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
	
	public Boolean getTaskOpen(){
		if(getStatus()==ToDoTaskStatus.OPEN || getStatus()==ToDoTaskStatus.GROUP_TASK) return true;
		else return false;
	}
	
	public Boolean getGroupTask(){
		if(getStatus()==ToDoTaskStatus.GROUP_TASK || getStatus()==ToDoTaskStatus.GROUP_TASK_CLOSED){
			return true;
		}
		else{
			return false;
		}
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	
	public String getRealizerGroupName() {
		if(getStatus()!=null && 
				(getStatus().equals(ToDoTaskStatus.GROUP_TASK) || getStatus().equals(ToDoTaskStatus.GROUP_TASK_CLOSED))){
			String[] tmp = this.description.split("#_#");
			return tmp[0];
		}else{
			return "no-group-name";			
		}
	}

	public String getPercentClosed() {
		return percentClosed;
	}

	public void setPercentClosed(String percentClosed) {
		this.percentClosed = percentClosed;
	}
	
	

	public Boolean getCanEdit() {
		return canEdit;
	}

	public void setCanEdit(Boolean canEdit) {
		this.canEdit = canEdit;
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
		final ToDoBean other = (ToDoBean) obj;
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

	@Override
	public int compareTo(ToDoBean arg0) {
		ToDoBean tdb = (ToDoBean)arg0;
		return getDeadline().compareTo(tdb.getDeadline());
	}
	
}
