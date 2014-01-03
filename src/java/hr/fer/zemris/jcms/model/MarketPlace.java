package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Model jedne "tržnice" grupa.
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="MarketPlace.list",query="select mp from MarketPlace as mp"),
    @NamedQuery(name="MarketPlace.listOffers",query="select o from MPOffer as o JOIN o.marketPlace.group as g where g.compositeCourseID LIKE :compositeCourseID and g.relativePath LIKE '0/%'")
})
@Entity
@Table(name="market_places")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MarketPlace implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private Group group;
	private boolean open;
	private Date openFrom;
	private Date openUntil;
	private String formulaConstraints;
	private String securityConstraints;
	private int timeBuffer;
	
	public MarketPlace() {
	}
	
	/**
	 * Identifikator grupe.
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
	 * Vrijeme relativno na početak termina grupe unutar kojega promjena više nije moguća.
	 * Vrijednost -1 znaći da se ovaj podatak ne uzima u obzir prilikom kontrole. 
	 * @return vremenski buffer
	 */
	public int getTimeBuffer() {
		return timeBuffer;
	}
	public void setTimeBuffer(int timeBuffer) {
		this.timeBuffer = timeBuffer;
	}
	
	@OneToOne(mappedBy="marketPlace")
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}

	public boolean getOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getOpenFrom() {
		return openFrom;
	}
	public void setOpenFrom(Date openFrom) {
		this.openFrom = openFrom;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getOpenUntil() {
		return openUntil;
	}
	public void setOpenUntil(Date openUntil) {
		this.openUntil = openUntil;
	}

	/**
	 * Ovo su ograničenja tipa:
	 * <pre>
	 * "1.R1" + "1.R3" + "1.R5" <= 240
	 * "1.R2" + "1.R4" + "1.R6" <= 240
	 * "2008-09-23 12:00 A101".demos <= 4 // jer zelimo max 4 demosa unutra...
	 * "2008-09-23 12:00 A101".student <= 30 // jer zelimo max 30 studenata unutra...
	 * </pre>
	 * @return
	 */
	@Column(nullable=true, length=32000)
	public String getFormulaConstraints() {
		return formulaConstraints;
	}
	public void setFormulaConstraints(String formulaConstraints) {
		this.formulaConstraints = formulaConstraints;
	}

	/**
	 * Ovo su ograničenja tipa:
	 * <pre>
	 * KLASICNO/STUDENT:KLASICNO/STUDENT,VHDLLAB/STUDENT:VHDLLAB/STUDENT,?/DEMOS:?/DEMOS
	 * </pre>
	 * Format je: izlaznaGrupa/tagStudenta:ulaznaGrupa/tagStudenta. Znak ? mijenja bilo što (pa i null),
	 * a znak # se baš odnosi na null odnosno prazno.
	 * @return
	 */
	@Column(nullable=true, length=1000)
	public String getSecurityConstraints() {
		return securityConstraints;
	}
	public void setSecurityConstraints(String securityConstraints) {
		this.securityConstraints = securityConstraints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getGroup() == null) ? 0 : getGroup().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MarketPlace))
			return false;
		MarketPlace other = (MarketPlace) obj;
		if (getGroup() == null) {
			if (other.getGroup() != null)
				return false;
		} else if (!getGroup().equals(other.getGroup()))
			return false;
		return true;
	}
	
	@Transient
	public boolean isActive(Date now) {
		if(!getOpen()) return false;
		if(getOpenFrom()!=null && now.before(getOpenFrom())) return false;
		if(getOpenUntil()!=null && now.after(getOpenUntil())) return false;
		return true;
	}
}
