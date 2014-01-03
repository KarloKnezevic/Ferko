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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@NamedQueries({
    @NamedQuery(name="CourseInstanceKeyValue.list",query="select kv from CourseInstanceKeyValue as kv where kv.courseInstance=:ci"),
    @NamedQuery(name="CourseInstanceKeyValue.load",query="select kv from CourseInstanceKeyValue as kv where kv.name=:key and kv.courseInstance=:ci")
})
@Entity
@Table(name="ci_keys",uniqueConstraints={@UniqueConstraint(columnNames={"courseInstance_id","name"})})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CourseInstanceKeyValue implements Serializable, Comparable<CourseInstanceKeyValue>  {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CourseInstance courseInstance;
	private String name;
	private String value;
	private int version;

	public CourseInstanceKeyValue() {
	}
	
	public CourseInstanceKeyValue(CourseInstance courseInstance, String name, String value) {
		super();
		if(name==null) throw new NullPointerException("Name can not be null!");
		if(courseInstance==null) throw new NullPointerException("CourseInstance can not be null!");
		this.courseInstance = courseInstance;
		this.name = name;
		this.value = value;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=50,nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(unique=false,length=4*1024*1024,nullable=true)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Version
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((courseInstance == null) ? 0 : courseInstance.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		CourseInstanceKeyValue other = (CourseInstanceKeyValue) obj;
		if (courseInstance == null) {
			if (other.courseInstance != null)
				return false;
		} else if (!courseInstance.equals(other.courseInstance))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(CourseInstanceKeyValue o) {
		if(o==null) return 1;
		return this.getName().compareToIgnoreCase(o.getName());
	}
}
