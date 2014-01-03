package hr.fer.zemris.jcms.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Detaljni opis korisnika.
 * 
 * @author marcupic
 *
 */
@Entity
@Table(name="user_activity_prefs")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserActivityPrefs implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String properties;
	
	/**
	 * Identifikator.
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=32000,nullable=true)
	public String getProperties() {
		return properties;
	}
	public void setProperties(String properties) {
		this.properties = properties;
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
		if (getClass() != obj.getClass())
			return false;
		UserActivityPrefs other = (UserActivityPrefs) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
