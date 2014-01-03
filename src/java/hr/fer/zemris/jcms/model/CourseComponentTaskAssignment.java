package hr.fer.zemris.jcms.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_task_assignment",uniqueConstraints={
	@UniqueConstraint(columnNames={"courseComponentTask_id","user_id"})	
})
@DiscriminatorValue("A")
@NamedQueries({
	// Pronalazi item opisan kolegijem kojem pripada, descriptorom kojem pripada i pozicijom na kojoj je
    @NamedQuery(name="CourseComponentTaskAssignment.findForItem",query="select new hr.fer.zemris.jcms.service.assessments.StudentTask(ccta.user.id,ccta.courseComponentTask.id,ccta.locked,ccta.lockingDate,ccta.reviewed,ccta.passed,ccta.score) from CourseComponentTaskAssignment ccta where ccta.courseComponentTask.courseComponentItem.courseComponent.courseInstance.id=:courseInstanceID and ccta.courseComponentTask.courseComponentItem.courseComponent.descriptor.shortName=:shortName and ccta.courseComponentTask.courseComponentItem.position=:position")
})
public class CourseComponentTaskAssignment extends USE2Element {

	private static final long serialVersionUID = 1L;
	
	private User user;
	private CourseComponentTask courseComponentTask;
	private boolean locked;
	private Date lockingDate;
	private Date extensionDate;
	private boolean reviewed;
	private User reviewedBy;
	private String comment;
	private boolean passed;
	private double score;
	private Set<CourseComponentTaskUpload> uploads = new HashSet<CourseComponentTaskUpload>();
	
	public CourseComponentTaskAssignment() {
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
	public CourseComponentTask getCourseComponentTask() {
		return courseComponentTask;
	}

	public void setCourseComponentTask(CourseComponentTask courseComponentTask) {
		this.courseComponentTask = courseComponentTask;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLockingDate() {
		return lockingDate;
	}

	public void setLockingDate(Date lockingDate) {
		this.lockingDate = lockingDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getExtensionDate() {
		return extensionDate;
	}

	public void setExtensionDate(Date extensionDate) {
		this.extensionDate = extensionDate;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public User getReviewedBy() {
		return reviewedBy;
	}

	public void setReviewedBy(User reviewedBy) {
		this.reviewedBy = reviewedBy;
	}

	@Column(length=1000)
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@OneToMany(mappedBy="courseComponentTaskAssignment",fetch=FetchType.LAZY)
	public Set<CourseComponentTaskUpload> getUploads() {
		return uploads;
	}

	public void setUploads(Set<CourseComponentTaskUpload> uploads) {
		this.uploads = uploads;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCourseComponentTask() == null) ? 0 : getCourseComponentTask()
						.hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponentTaskAssignment))
			return false;
		final CourseComponentTaskAssignment other = (CourseComponentTaskAssignment) obj;
		if (getCourseComponentTask() == null) {
			if (other.getCourseComponentTask() != null)
				return false;
		} else if (!getCourseComponentTask().equals(other.getCourseComponentTask()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}

	
	
}
