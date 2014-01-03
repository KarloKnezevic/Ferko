package hr.fer.zemris.jcms.model.planning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Pohrana čistih podataka planova i gotovih rasporeda
 */
 
@Entity
@Table(name="planning_data")

public class PlanningStorage{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String data;
	private int version;

	public PlanningStorage(){
	}
	
	public PlanningStorage(String data) {
		this.data = data;
	}
	
	/**
	 * Identifikator podatka
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
	 * Podaci plana/rasporeda
	 * @return
	 */
	@Column(length=2000000,nullable=false)
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
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
		if (!(obj instanceof PlanningStorage))
			return false;
		PlanningStorage other = (PlanningStorage) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
