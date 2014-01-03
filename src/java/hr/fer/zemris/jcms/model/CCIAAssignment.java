package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="CCIAAssignment")
public class CCIAAssignment implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private User user;
	private CourseComponentItemAssessment courseComponentItemAssessment;
	
	public CCIAAssignment() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponentItemAssessment getCourseComponentItemAssessment() {
		return courseComponentItemAssessment;
	}

	public void setCourseComponentItemAssessment(
			CourseComponentItemAssessment courseComponentItemAssessment) {
		this.courseComponentItemAssessment = courseComponentItemAssessment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCourseComponentItemAssessment() == null) ? 0
						: getCourseComponentItemAssessment().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CCIAAssignment))
			return false;
		final CCIAAssignment other = (CCIAAssignment) obj;
		if (getCourseComponentItemAssessment() == null) {
			if (other.getCourseComponentItemAssessment() != null)
				return false;
		} else if (!getCourseComponentItemAssessment()
				.equals(other.getCourseComponentItemAssessment()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
	
	
}
