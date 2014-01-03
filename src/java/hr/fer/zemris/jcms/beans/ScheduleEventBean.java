package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.beans.ScheduleTermBean;
import hr.fer.zemris.util.scheduling.support.IScheduleEvent;

import java.util.ArrayList;
import java.util.List;

public class ScheduleEventBean{
	private String name;
	private String id;
	private List<ScheduleTermBean> termBeans;
	
	//Tip grupe u koju treba objaviti događaj - privategroup ili componentgroup (default)
	private String groupType = IScheduleEvent.COMPONENT_GROUP;
	//Podaci o odabranoj grupi komponente
	private String componentID;
	private String selectedNumber;
	//Podaci o privatnoj grupi
	private String privateGroupName;
	
	public ScheduleEventBean(){
		termBeans = new ArrayList<ScheduleTermBean>();
	}
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ScheduleTermBean> getTermBeans() {
		return termBeans;
	}
	public void setTermBeans(List<ScheduleTermBean> termBeans) {
		this.termBeans = termBeans;
	}
	public String getComponentID() {
		return componentID;
	}
	public void setComponentID(String componentID) {
		this.componentID = componentID;
	}
	public String getSelectedNumber() {
		return selectedNumber;
	}
	public void setSelectedNumber(String selectedNumber) {
		this.selectedNumber = selectedNumber;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getPrivateGroupName() {
		return privateGroupName;
	}

	public void setPrivateGroupName(String privateGroupName) {
		this.privateGroupName = privateGroupName;
	}
	
	
	
	
}