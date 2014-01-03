package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@DiscriminatorValue("F")
public class CourseComponentFDef extends AbstractCourseComponentDef {

	private static final long serialVersionUID = 1L;
	
	private AssessmentFlag assessmentFlag;
	
	public CourseComponentFDef() {
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public AssessmentFlag getAssessmentFlag() {
		return assessmentFlag;
	}
	public void setAssessmentFlag(AssessmentFlag assessmentFlag) {
		this.assessmentFlag = assessmentFlag;
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
		if (!(obj instanceof AbstractCourseComponentDef))
			return false;
		final AbstractCourseComponentDef other = (AbstractCourseComponentDef) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
