package hr.fer.zemris.jcms.model.planning;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Generirani raspored
 */
 
@Entity
@Table(name="schedules")
@NamedQueries({
    @NamedQuery(name="ScheduleDescriptor.listForPlan",query="select m from ScheduleDescriptor as m where m.parent.id = :planID")
})
public class ScheduleDescriptor implements Comparable<ScheduleDescriptor>{

	private static final long serialVersionUID = 1L;

	private Long id;
	private PlanningStorage data;
	private PlanDescriptor parent;
	private Date creationDate; 
	private String parameters;
	private int version;

	
	public ScheduleDescriptor() {
	}
	
	/**
	 * Identifikator rasporeda
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
	 * Sadržaj generiranog rasporeda
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY)
	public PlanningStorage getData() {
		return data;
	}
	public void setData(PlanningStorage data) {
		this.data = data;
	}
	
	/**
	 * Vrijeme i datum generiranja rasporeda
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
	 * Plan kojem pripada raspored
	 * @return
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public PlanDescriptor getParent() {
		return parent;
	}
	public void setParent(PlanDescriptor parent) {
		this.parent = parent;
	}

	/**
	 * Parametri algoritma za izradu rasporeda
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
	public int compareTo(ScheduleDescriptor o) {
		return this.getCreationDate().compareTo(o.getCreationDate());
	}



}
