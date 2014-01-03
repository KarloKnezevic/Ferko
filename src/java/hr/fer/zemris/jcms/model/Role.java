package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Modeliranje uloge korisnika u sustavu.
 * 
 * @author marcupic
 *
 */
@Entity
@Table(name="roles")
@NamedQueries({
	@NamedQuery(name="Role.list",query="Select r from Role as r"),
	@NamedQuery(name="role.listWithRole",query="Select u from User as u join u.userDescriptor as ud join ud.roles as r where r.name=:roleName"),
	@NamedQuery(name="role.listWithRole2",query="Select u from User as u join u.userDescriptor as ud join ud.roles as r where r.name=:roleName AND u.lastName LIKE :lastName"),
	@NamedQuery(name="role.listWithRole3",query="Select u from User as u join u.userDescriptor as ud join ud.roles as r where r.name=:roleName AND u.lastName=:lastName AND u.firstName LIKE :firstName"),
	@NamedQuery(name="role.listWithRole4",query="Select u from User as u join u.userDescriptor as ud join ud.roles as r where r.name=:roleName AND u.lastName=:lastName AND u.firstName=:firstName AND u.jmbag LIKE :jmbag")
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Role implements java.io.Serializable, Comparable<Role> {
	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private int version;
	private String compositeCourseID;
	
	public Role() {
	}

	public Role(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	/**
	 * Ime. Ovo je ujedno i ključ, tj. jedinstvena vrijednost. 
	 * @return
	 */
	@Id @Column(length=20)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Opis uloge.
	 * 
	 * @return
	 */
	@Column(nullable=false,length=50)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Kompozitni identifikator kolegija nad kojim je korisniku
	 * postavljena uloga. Za detalje vidi opis samog razreda {@linkplain Group}.
	 * @return
	 */
	@Column(nullable=true,length=16,unique=false)
	// TODO: Možda dodati kao ForeiginKey?
	public String getCompositeCourseID() {
		return compositeCourseID;
	}
	
	public void setCompositeCourseID(String compositeCourseID) {
		this.compositeCourseID = compositeCourseID;
	}
	
	/**
	 * Verzija. Podrška za optimistično zaključavanje. 
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
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Role))
			return false;
		final Role other = (Role) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Role o) {
		return this.getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
