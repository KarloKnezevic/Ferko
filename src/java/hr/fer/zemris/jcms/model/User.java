package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.exceptions.NoPermissionException;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Model korisnika. Sadrži minimalnu količinu informacija kako ne bi opterećivao memoriju.
 * Detalji koji su rijeđe potrebni mogu se dohvatiti kroz {@linkplain #getUserDescriptor()}.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="User.findByUsernameFull",query="select u from User as u join fetch u.userDescriptor where u.username=:username"),
    @NamedQuery(name="User.findByUsername",query="select u from User as u where u.username=:username"),
    @NamedQuery(name="User.findByJMBAG",query="select u from User as u where u.jmbag=:jmbag")
})
@Entity
@Table(name="users")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String jmbag;
	private String firstName;
	private String lastName;
	private String username;
	private UserDescriptor userDescriptor;
	private int version;
	
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

	/**
	 * JMBAG korisnika. problem su korisnici koji nisu studenti - što njima staviti
	 * ovdje? Kako ovo polje mora biti unique, definirano je kao 11 znamenkasto, pa
	 * ne-studenti možda mogu dobivati neke druge jedinstvene oznake (tipa N..........).
	 * Još za razmisliti kako ovo ostvariti. 
	 * @return
	 */
	@Column(length=25,nullable=false,unique=true)
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	/**
	 * Ime osobe.
	 * @return
	 */
	@Column(length=40,nullable=false,unique=false)
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * Prezime osobe.
	 * @return
	 */
	@Column(length=40,nullable=false,unique=false)
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * Korisničko ime kojim se osoba prijavljuje na sustav. Ne mora
	 * odgovarati autentifikacijskom korisničkom imenu.
	 * @return
	 */
	@Column(length=30,nullable=false,unique=true)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Detaljniji opis korisnika.
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public UserDescriptor getUserDescriptor() {
		return userDescriptor;
	}
	public void setUserDescriptor(UserDescriptor userDescriptor) {
		this.userDescriptor = userDescriptor;
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
	
	/**
	 * Dodavanje nove dozvole.
	 * 
	 * @param actionPermissions Skup dozvoljenih akcija nad grupom.
	 * @param groupFullPath Grupa nad kojom vrijedi dozvola.
	 */
	public void addPermission(Set<String> actionPermissions, String groupFullPath) {
		getUserDescriptor().getPermissions().add(new Permission(actionPermissions, groupFullPath));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getJmbag() == null) ? 0 : getJmbag().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		if (getJmbag() == null) {
			if (other.getJmbag() != null)
				return false;
		} else if (!getJmbag().equals(other.getJmbag()))
			return false;
		return true;
	}
}
