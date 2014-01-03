package hr.fer.zemris.jcms.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Model lokacije. Lokacija može imati jednu ili više soba ({@linkplain Room}.
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="Venue.list",query="select v from Venue as v")
})
@Entity
@Table(name="venues")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Venue {
	private String shortName;
	private String name;
	private String address;
	private String locator;
	private Set<Room> rooms = new HashSet<Room>();
	
	public Venue() {
	}

	/**
	 * Naziv lokacije.
	 * 
	 * @return naziv
	 */
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Kratki naziv lokacije (npr. FER). Ovo je ujedno i primarni ključ.
	 * 
	 * @return kratki naziv
	 */
	@Id
	@Column(length=10)
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	/**
	 * Adresa lokacije. Omogućiti unos više redaka teksta.
	 * 
	 * @return adresa
	 */
	@Column(length=200,nullable=false)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Lokator pomoću kojega se može doći do mape ili drugog načina određivanja
	 * pozicije navedene prostorije.
	 * 
	 * @return lokator
	 */
	@Column(length=100,nullable=true)
	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	/**
	 * Popis svih soba koje se nalaze na ovoj lokaciji.
	 * 
	 * @return skup soba
	 */
	@OneToMany(fetch=FetchType.LAZY,mappedBy="venue",cascade=CascadeType.ALL)
	public Set<Room> getRooms() {
		return rooms;
	}
	public void setRooms(Set<Room> rooms) {
		this.rooms = rooms;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Venue))
			return false;
		Venue other = (Venue) obj;
		if (getAddress() == null) {
			if (other.getAddress() != null)
				return false;
		} else if (!getAddress().equals(other.getAddress()))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	
}
