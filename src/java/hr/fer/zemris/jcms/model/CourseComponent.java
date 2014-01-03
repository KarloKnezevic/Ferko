package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_components", uniqueConstraints={
	@UniqueConstraint(columnNames={"courseInstance_id","descriptor_id"})
})
@NamedQueries({
    @NamedQuery(name="CourseComponent.listOnCourse",query="select cc from CourseComponent as cc where cc.courseInstance=:courseInstance")
})
public class CourseComponent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CourseInstance courseInstance;
	private CourseComponentDescriptor descriptor;
	private Set<CourseComponentItem> items = new HashSet<CourseComponentItem>();
	
	public CourseComponent() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Kojem kolegiju komponenta pripada
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	/**
	 * Deskriptor koji opisuje komponentu
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponentDescriptor getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(CourseComponentDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * Svi itemi ove komponente
	 * @return
	 */
	@OneToMany(mappedBy="courseComponent",fetch=FetchType.EAGER)
	@OrderBy("position")
	public Set<CourseComponentItem> getItems() {
		return items;
	}
	public void setItems(Set<CourseComponentItem> items) {
		this.items = items;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getCourseInstance() == null) ? 0 : getCourseInstance().hashCode());
		result = prime * result
				+ ((getDescriptor() == null) ? 0 : getDescriptor().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponent))
			return false;
		final CourseComponent other = (CourseComponent) obj;
		if (getCourseInstance() == null) {
			if (other.getCourseInstance() != null)
				return false;
		} else if (!getCourseInstance().equals(other.getCourseInstance()))
			return false;
		if (getDescriptor() == null) {
			if (other.getDescriptor() != null)
				return false;
		} else if (!getDescriptor().equals(other.getDescriptor()))
			return false;
		return true;
	}
}
