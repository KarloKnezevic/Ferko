package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_item_assessment")
@NamedQueries({
    @NamedQuery(name="CourseComponentItemAssessment.getItemAssessmentUsers",query="select cciaa from CCIAAssignment as cciaa where cciaa.courseComponentItemAssessment=:courseComponentItemAssessment"),
    @NamedQuery(name="CourseComponentItemAssessment.getItemAssessmentAssignment",query="select cciaa from CCIAAssignment as cciaa where cciaa.courseComponentItemAssessment=:courseComponentItemAssessment and cciaa.user=:user"),
    @NamedQuery(name="CourseComponentItemAssessment.listUserAssessments",query="select cciaa.courseComponentItemAssessment from CCIAAssignment as cciaa where cciaa.courseComponentItemAssessment.courseComponentItem=:courseComponentItem AND cciaa.user=:user"),
    @NamedQuery(name="CourseComponentItemAssessment.getForAssessment",query="select ccia from CourseComponentItemAssessment as ccia where ccia.assessment=:assessment")
})
public class CourseComponentItemAssessment implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String assessmentIdentifier;
	private CourseComponentItem courseComponentItem;
	private Assessment assessment;
	
	public CourseComponentItemAssessment() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=200,nullable=false)
	public String getAssessmentIdentifier() {
		return assessmentIdentifier;
	}

	public void setAssessmentIdentifier(String assessmentIdentifier) {
		this.assessmentIdentifier = assessmentIdentifier;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponentItem getCourseComponentItem() {
		return courseComponentItem;
	}

	public void setCourseComponentItem(CourseComponentItem courseComponentItem) {
		this.courseComponentItem = courseComponentItem;
	}

	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getAssessmentIdentifier() == null) ? 0 : getAssessmentIdentifier()
						.hashCode());
		result = prime
				* result
				+ ((getCourseComponentItem() == null) ? 0 : getCourseComponentItem()
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponentItemAssessment))
			return false;
		final CourseComponentItemAssessment other = (CourseComponentItemAssessment) obj;
		if (getAssessmentIdentifier() == null) {
			if (other.getAssessmentIdentifier() != null)
				return false;
		} else if (!getAssessmentIdentifier().equals(other.getAssessmentIdentifier()))
			return false;
		if (getCourseComponentItem() == null) {
			if (other.getCourseComponentItem() != null)
				return false;
		} else if (!getCourseComponentItem().equals(other.getCourseComponentItem()))
			return false;
		return true;
	}

	
}
