package hr.fer.zemris.jcms.model.planning;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.PlanStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

/**
 * Plan / konfiguracija rasporeda
 * 
 */
 
@Entity
@Table(name="plans")
@NamedQueries({
    @NamedQuery(name="PlanDescriptor.listPlansForUserOnCourse",query="select m from PlanDescriptor as m where m.courseInstance.id = :courseInstanceID AND m.owner.id = :userID")
})

public class PlanDescriptor implements Comparable<PlanDescriptor>{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private PlanningStorage planData;
	private PlanningStorage peopleData;
	private PlanningStorage termData;
	private User owner;
	private PlanStatus status;
	private Date creationDate; 
	private List<ScheduleDescriptor> schedules = new ArrayList<ScheduleDescriptor>();
	private CourseInstance courseInstance;
	private String parameters;
	private int version;

	public PlanDescriptor() {
	}
	
	/**
	 * Identifikator plana
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Naziv plana
	 * @return
	 */
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sadržaj plana - konfiguracija rasporeda
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public PlanningStorage getPlanData() {
		return planData;
	}
	public void setPlanData(PlanningStorage data) {
		this.planData = data;
	}
	
	/**
	 * Podaci o zauzećima osoba
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public PlanningStorage getPeopleData() {
		return peopleData;
	} 
	public void setPeopleData(PlanningStorage data) {
		this.peopleData = data;
	}
	
	/**
	 * Podaci o terminima/prostorijama
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public PlanningStorage getTermData() {
		return termData;
	}
	public void setTermData(PlanningStorage data) {
		this.termData = data;
	}
	
	/**
	 * Vlasnik/autor konfiguracije
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}

	/**
	 * Status plana
	 * @return
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public PlanStatus getStatus() {
		return status;
	}
	
	public void setStatus(PlanStatus status) {
		this.status = status;
	}
	
	/**
	 * Generirani rasporedi za ovaj plan/konfiguraciju
	 * @return
	 */
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public List<ScheduleDescriptor> getSchedules() {
		return schedules;
	}
	public void setSchedules(List<ScheduleDescriptor> schedules) {
		this.schedules = schedules;
	}

	/**
	 * Kolegij kojem pripada plan
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	/**
	 * Parametri algoritmu za generiranje plana
	 * @return
	 */
	@Column(length=100,nullable=false)
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Vrijeme i datum stvaranja plana
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}
	
	/**
	 * Version. Optimistic lock support.
	 * @return
	 */
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
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PlanDescriptor))
			return false;
		PlanDescriptor other = (PlanDescriptor) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public int compareTo(PlanDescriptor o) {
		//TODO: Implementirati po potrebi
		return 0;
	}



}
