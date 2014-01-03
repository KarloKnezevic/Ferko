package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_items")
@NamedQueries({
	// Pronalazi item opisan kolegijem kojem pripada, descriptorom kojem pripada i pozicijom na kojoj je
    @NamedQuery(name="CourseComponentItem.findForCDP",query="select cci from CourseComponentItem as cci where cci.courseComponent.courseInstance.id=:courseInstanceID and cci.courseComponent.descriptor.groupRoot=:groupRoot and cci.position=:position")
})
public class CourseComponentItem implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private CourseComponent courseComponent;
	private int position;
	private String name;
	private Assessment assessment;
	private Set<CourseComponentTask> tasks = new HashSet<CourseComponentTask>();
	private Set<CourseComponentItemAssessment> itemAssessments = new HashSet<CourseComponentItemAssessment>();
	private Set<AbstractCourseComponentDef> courseComponentDefs = new HashSet<AbstractCourseComponentDef>();
	private Set<ItemDescriptionFile> itemDescriptionFiles = new HashSet<ItemDescriptionFile>();
	private Group group;
	
	public CourseComponentItem() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Kojoj komponenti ovo pripada
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponent getCourseComponent() {
		return courseComponent;
	}

	public void setCourseComponent(CourseComponent courseComponent) {
		this.courseComponent = courseComponent;
	}

	/**
	 * Redni broj komponente
	 * @return
	 */
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Ime itema (primjerice: Kombinacijski sklopovi)
	 * @return
	 */
	@Column(length=150,nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Opcionalna provjera koja skuplja bodove s taskova
	 * @return
	 */
	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public Assessment getAssessment() {
		return assessment;
	}
	
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@OneToMany(mappedBy="courseComponentItem",fetch=FetchType.LAZY)
	public Set<CourseComponentTask> getTasks() {
		return tasks;
	}

	public void setTasks(Set<CourseComponentTask> tasks) {
		this.tasks = tasks;
	}
	
	@OneToMany(mappedBy="courseComponentItem",fetch=FetchType.LAZY)
	public Set<CourseComponentItemAssessment> getItemAssessments() {
		return itemAssessments;
	}

	public void setItemAssessments(
			Set<CourseComponentItemAssessment> itemAssessments) {
		this.itemAssessments = itemAssessments;
	}

	@OneToMany(mappedBy="courseComponentItem",fetch=FetchType.LAZY)
	public Set<AbstractCourseComponentDef> getCourseComponentDefs() {
		return courseComponentDefs;
	}

	public void setCourseComponentDefs(Set<AbstractCourseComponentDef> courseComponentDefs) {
		this.courseComponentDefs = courseComponentDefs;
	}

	@OneToMany(mappedBy="courseComponentItem",fetch=FetchType.LAZY)
	public Set<ItemDescriptionFile> getItemDescriptionFiles() {
		return itemDescriptionFiles;
	}

	public void setItemDescriptionFiles(Set<ItemDescriptionFile> itemDescriptionFiles) {
		this.itemDescriptionFiles = itemDescriptionFiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getCourseComponent() == null) ? 0 : getCourseComponent().hashCode());
		result = prime * result + getPosition();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponentItem))
			return false;
		final CourseComponentItem other = (CourseComponentItem) obj;
		if (getCourseComponent() == null) {
			if (other.getCourseComponent() != null)
				return false;
		} else if (!getCourseComponent().equals(other.getCourseComponent()))
			return false;
		if (getPosition() != other.getPosition())
			return false;
		return true;
	}

	
}
