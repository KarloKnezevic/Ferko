package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.extra.ApplicationStatus;

import java.util.Date;

public class StudentApplicationShortBean {
	private Long userID;
	private String status;
	private Date date;
	private String detailedData;
	
	public StudentApplicationShortBean(Long userID, String status, Date date, String detailedData) {
		super();
		this.userID = userID;
		setStatus(status);
		this.date = date;
		this.detailedData = detailedData;
	}
	
	public StudentApplicationShortBean(Long userID, ApplicationStatus status, Date date, String detailedData) {
		super();
		this.userID = userID;
		setStatus(status==null ? null : status.toString());
		this.date = date;
		this.detailedData = detailedData;
	}
	
	public StudentApplicationShortBean() {
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		if(status!=null) {
			if(status.equals("NEW")) {
				this.status = "NEW";
			} else
			if(status.equals("ACCEPTED")) {
				this.status = "ACCEPTED";
			} else
			if(status.equals("REJECTED")) {
				this.status = "REJECTED";
			} else {
				this.status = status;
			}
		} else {
			this.status = "";
		}
	}

	public String getDetailedData() {
		return detailedData;
	}
	public void setDetailedData(String detailedData) {
		this.detailedData = detailedData;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
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
		StudentApplicationShortBean other = (StudentApplicationShortBean) obj;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		return true;
	}
}
