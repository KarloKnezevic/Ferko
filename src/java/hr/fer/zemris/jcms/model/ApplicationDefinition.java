package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedQueries({
    @NamedQuery(name="ApplicationDefinition.list",query="select ad from ApplicationDefinition as ad where ad.course.id=:courseID"),
    @NamedQuery(name="ApplicationDefinition.listForCIAndSN",query="select ad from ApplicationDefinition as ad where ad.course=:courseInstance and ad.shortName=:shortName")
})
@Entity
@Table(name="application_definitions", uniqueConstraints={
		@UniqueConstraint(columnNames={"course_id","shortName"})
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ApplicationDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String shortName;
	private Date openFrom;
	private Date openUntil;
	private CourseInstance course;
	private int programVersion;
	private String program;
	
	public ApplicationDefinition() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(length=10,nullable=false)
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getOpenFrom() {
		return openFrom;
	}

	public void setOpenFrom(Date date) {
		this.openFrom = date;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getOpenUntil() {
		return openUntil;
	}

	public void setOpenUntil(Date date) {
		this.openUntil = date;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	public CourseInstance getCourse() {
		return course;
	}

	public void setCourse(CourseInstance course) {
		this.course = course;
	}

	public int getProgramVersion() {
		return programVersion;
	}
	public void setProgramVersion(int programVersion) {
		this.programVersion = programVersion;
	}
	
	@Column(length=32000,nullable=true)
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
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
		if (!(obj instanceof ApplicationDefinition))
			return false;
		ApplicationDefinition other = (ApplicationDefinition) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
}