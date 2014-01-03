package hr.fer.zemris.jcms.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Plan bean
 * @author IvanFer
 */
public class ScheduleBean implements Comparable<ScheduleBean>{

	private String id;
	private String name;
	private String status;
	private String creationDate;
	private String parameters;
	private String publicationDate;

	private List<ScheduleEventBean> eventBeans; //koristi se u objavi rasporeda
	private Date date;
	
	public ScheduleBean(){
		eventBeans = new ArrayList<ScheduleEventBean>();
	}

	public ScheduleBean(String scheduleID){
		this.id=scheduleID;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getParameters() {
		return parameters;
	}


	public List<ScheduleEventBean> getEventBeans() {
		return eventBeans;
	}

	public void setEventBeans(List<ScheduleEventBean> eventBeans) {
		this.eventBeans = eventBeans;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduleBean other = (ScheduleBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id)){
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)){
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(ScheduleBean arg0) {
		return this.creationDate.compareTo(arg0.creationDate);
	}





}
