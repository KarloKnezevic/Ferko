package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_def")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="dtype",discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("*")
public abstract class AbstractCourseComponentDef implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private CourseComponentItem courseComponentItem;
	private int position;
	
	public AbstractCourseComponentDef() {
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
	public CourseComponentItem getCourseComponentItem() {
		return courseComponentItem;
	}

	public void setCourseComponentItem(CourseComponentItem courseComponentItem) {
		this.courseComponentItem = courseComponentItem;
	}

	@Column(nullable=false)
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
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
