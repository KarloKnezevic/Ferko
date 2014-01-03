package hr.fer.zemris.jcms.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.model.planning.PlanDescriptor;
import hr.fer.zemris.jcms.service.PlanningService;
import hr.fer.zemris.jcms.web.actions.data.PlanningData;

/**
 * Plan bean
 * @author IvanFer
 */
public class PlanDescriptorBean implements Comparable<PlanDescriptorBean>{

	private Long id;
	private String name;
	private String status;
	private String creationDate;
	private String parameters;
	private List<ScheduleBean> schedules; 
	private Date date;
	
	public PlanDescriptorBean(){
		
	}

	public PlanDescriptorBean(PlanDescriptor p, PlanningData data) {
		this.id=p.getId();
		this.name=p.getName();
		switch(p.getStatus()){
			case NEW : setStatus(data.getMessageLogger().getText("Planning.planStatusNew"));break;
			case PREPARING : setStatus(data.getMessageLogger().getText("Planning.planStatusPreparing"));break;
			case PREPARATION_ERROR : setStatus(data.getMessageLogger().getText("Planning.planStatusPreparationError"));break;
			case PREPARED : setStatus(data.getMessageLogger().getText("Planning.planStatusPrepared"));break;
			case SEARCHING : setStatus(data.getMessageLogger().getText("Planning.planStatusSearching"));break;
			case COMPLETED : setStatus(data.getMessageLogger().getText("Planning.planStatusCompleted"));break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(PlanningService.DATE_FORMAT);
		this.creationDate=sdf.format(p.getCreationDate());
		this.date = p.getCreationDate();
		this.parameters=p.getParameters();
	}

	public void setID(Long id) {
		this.id = id;
	}

	public Long getID() {
		return id;
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

	
	public List<ScheduleBean> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<ScheduleBean> schedules) {
		this.schedules = schedules;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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
		PlanDescriptorBean other = (PlanDescriptorBean) obj;
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
	public int compareTo(PlanDescriptorBean arg0) {
		return -1 * this.date.compareTo(arg0.date);
	}



}