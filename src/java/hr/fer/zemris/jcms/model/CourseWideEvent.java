package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Ovo je dogadaj na razini odredenog kolegija.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="CourseWideEvent.findForUser",query="select cwe from CourseWideEvent as cwe, UserGroup as ug, CourseInstance ci WHERE cwe.hidden=false AND :user=ug.user AND (ug.group.relativePath LIKE '0/%' OR ug.group.relativePath LIKE '3/%') AND ci.id=ug.group.compositeCourseID and cwe.courseInstance=ci"),
    @NamedQuery(name="CourseWideEvent.findForUser2",query="select cwe from CourseWideEvent as cwe, UserGroup as ug, CourseInstance ci WHERE cwe.hidden=false AND :user=ug.user AND (ug.group.relativePath LIKE '0/%' OR ug.group.relativePath LIKE '3/%') AND ci.id=ug.group.compositeCourseID and cwe.courseInstance=ci AND cwe.start >= :fromDate AND cwe.start <= :toDate"),
    @NamedQuery(name="CourseWideEvent.findForCourseInstance",query="select cwe from CourseWideEvent as cwe WHERE cwe.hidden=false AND cwe.courseInstance=:courseInstance"),
    @NamedQuery(name="CourseWideEvent.findForCourseInstance2",query="select cwe from CourseWideEvent as cwe WHERE cwe.hidden=false AND cwe.courseInstance=:courseInstance AND cwe.start >= :fromDate AND cwe.start <= :toDate")
})
@Entity
@DiscriminatorValue("C")
public class CourseWideEvent extends AbstractEvent {
	
	private static final long serialVersionUID = 1L;

	private CourseInstance courseInstance;
	
	public CourseWideEvent() {
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=true)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseWideEvent))
			return false;
		final CourseWideEvent other = (CourseWideEvent) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
