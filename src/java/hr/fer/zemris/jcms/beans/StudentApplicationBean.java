package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.applications.model.ApplElement;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentApplicationBean {
	
	private Long id;
	private Long userID;
	private Date date;
	private String status;
	private String reason;
	private String statusReason;
	private String definition;
	private List<ApplElement> elements;
	private Map<String,Object> map;
	private String state;
	
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getStatusReason() {
		return statusReason;
	}
	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public List<ApplElement> getElements() {
		return elements;
	}
	public void setElements(List<ApplElement> elements) {
		this.elements = elements;
	}
	
	public Map<String, Object> getMap() {
		if(map==null) {
			map = new HashMap<String, Object>();
		}
		return map;
	}
	public void setMap(Map<String, Object> map) {
		this.map = map;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
