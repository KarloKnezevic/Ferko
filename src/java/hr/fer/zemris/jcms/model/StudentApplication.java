package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.extra.ApplicationStatus;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@NamedQueries({
    @NamedQuery(name="StudentApplication.listForCourseInstanceAndSN",query="select new hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean(sa.user.id, sa.status, sa.date, sa.detailedData) from StudentApplication as sa where sa.applicationDefinition.course=:courseInstance and sa.applicationDefinition.shortName=:shortName"),
    @NamedQuery(name="StudentApplication.list",query="select sa from StudentApplication as sa where sa.applicationDefinition.course.id=:courseID"),
    @NamedQuery(name="StudentApplication.listForUser",query="select sa from StudentApplication as sa where sa.user=:user and sa.applicationDefinition.course.id=:courseID"),
    @NamedQuery(name="StudentApplication.getForUser",query="select sa from StudentApplication as sa where sa.user=:user and sa.applicationDefinition.id=:defID"),
    @NamedQuery(name="StudentApplication.listForDefinition",query="select sa from StudentApplication as sa where sa.applicationDefinition.course.id=:courseID and sa.applicationDefinition.id=:defID")
})
@Entity
@Table(name="student_applications", uniqueConstraints={
	@UniqueConstraint(columnNames={"user_id", "applicationDefinition_id"})
})
public class StudentApplication implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private User user;
	private ApplicationDefinition applicationDefinition;
	private Date date;
	private ApplicationStatus status = ApplicationStatus.NEW;
	private String reason;
	private String statusReason;
	private String detailedData;
	
	public StudentApplication() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	public ApplicationDefinition getApplicationDefinition() {
		return applicationDefinition;
	}

	public void setApplicationDefinition(ApplicationDefinition applicationDefinition) {
		this.applicationDefinition = applicationDefinition;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	@Enumerated
	public ApplicationStatus getStatus() {
		return status;
	}
	
	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	@Column(length=1000,nullable=false)
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Column(length=1000)
	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	
	@Column(length=32000,nullable=true)
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StudentApplication))
			return false;
		StudentApplication other = (StudentApplication) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	
}
