package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Model taga (oznake) koja se moze lijepiti na zastavicu.
 * Svaka zastavica može imati samo jedan tag. Primjeri tagova
 * su:
 * <ul>
 * <li>Ostvario prolaz na kolegiju</li>
 * </ul>
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="AssessmentFlagTag.list",query="select a from AssessmentFlagTag as a"),
    @NamedQuery(name="AssessmentFlagTag.findByShortName",query="select a from AssessmentFlagTag as a where a.shortName=:shortName")
})
@Entity
@Table(name="assessment_flag_tags")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AssessmentFlagTag implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private boolean active;
	private String shortName;
	
	public AssessmentFlagTag() {
	}

	public AssessmentFlagTag(boolean active, Long id, String name,
			String shortName) {
		super();
		this.active = active;
		this.id = id;
		this.name = name;
		this.shortName = shortName;
	}

	/**
	 * Identifikator.
	 * 
	 * @return identifikator
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Je li oznaka aktivna (true). Aktivne oznake se prikazuju
	 * korisnicima i oni ih mogu odabrati. Neaktivne se ne prikazuju.
	 * @return true ako je oznaka aktivna, false inače
	 */
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Naziv oznake.
	 * 
	 * @return naziv
	 */
	@Column(length=50, unique=true, nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length=10, unique=true, nullable=false)
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentFlagTag))
			return false;
		AssessmentFlagTag other = (AssessmentFlagTag) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
}
