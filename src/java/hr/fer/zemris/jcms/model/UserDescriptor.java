package hr.fer.zemris.jcms.model;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

/**
 * Detaljni opis korisnika.
 * 
 * @author marcupic
 *
 */
@Entity
@Table(name="user_descriptors")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserDescriptor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String authUsername;
	private String password;
	private String email;
	private AuthType authType;
	private boolean dataValid;
	private boolean locked;
	private Set<Role> roles = new HashSet<Role>();
	private Set<Permission> permissions = new HashSet<Permission>(); // TODO: Ukloniti...
	private int version;
	private String externalID;
	private UserActivityPrefs userActivityPrefs;
	
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
	 * Polje externalID nije unique! Ono sto ce se slati klijentu kao
	 * externalID jest identifikator u hex formatu, dvotocka, pa ovaj
	 * dio koji zapravo sluzi kao sifra. Dakle:
	 * Long.toString(getId(),16)+":"+getExternalID().
	 * Kada korisnik ovo zatrazi, splita se po ':', lijevi dio isparsira
	 * u Long, dohvati po tom ID-u UserDescriptor i zatim usporedi zapisanu
	 * "sifru", tj. externalID sa desnim dijelom splitanog.
	 * @return
	 */
	@Index(name="udes_externalid_index")
	@Column(length=64, nullable=false)
	public String getExternalID() {
		return externalID;
	}
	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}
	
	/**
	 * Jesu li podaci valjani? Ako nisu, korisniku sustav neće dozvoliti da se prijavi.
	 * Ovo je potrebno kada je korisnici u sustav unose po dijelovima (recimo, znam sve
	 * osim logina, pa privremeno zapišem jmbag i na to mjesto). Tek kada nabavim i login
	 * (tj. username), postavljam ovo na true.
	 * 
	 * @return
	 */
	public boolean getDataValid() {
		return dataValid;
	}
	public void setDataValid(boolean dataValid) {
		this.dataValid = dataValid;
	}

	/**
	 * Je li korisnik zaključan. Ako je, sustav mu ne dopušta prijavu.
	 * 
	 * @return
	 */
	public boolean getLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	/**
	 * Korisničko ime koje se koristi za autentifikaciju.
	 * @return
	 */
	@Column(length=30,nullable=false,unique=false)
	public String getAuthUsername() {
		return authUsername;
	}
	public void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}
	
	/**
	 * Lokalna šifra korisnika. Ovisno o konfiguriranom načinu provjere
	 * identiteta korisnika, ovo se može koristiti ili ne mora. Primjerice,
	 * ako autentifikacija ide preko FERWeb-a, tada se ovo neće koristiti.
	 * @return
	 */
	@Column(length=64,nullable=false,unique=false)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * E-mail adresa korisnika.
	 * @return
	 */
	@Column(length=60,nullable=true,unique=false)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Način na koji se obavlja provjera identiteta korisnika.
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER,cascade={CascadeType.PERSIST})
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public AuthType getAuthType() {
		return authType;
	}
	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	/**
	 * Dohvati sve uloge koje korisnik ima u sustavu.
	 * @return
	 */
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
			name="user_roles",
			joinColumns=@JoinColumn(name="user_descriptor_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="role_name",referencedColumnName="name")
	)
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	/**
	 * Dohvati sva dopuštenja koja korisnik ima u sustavu.
	 * 
	 * @return Sve korisnikove dozvole.
	 */
	@ManyToMany(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable(name="user_to_permission",
			joinColumns=@JoinColumn(name="user_descriptor_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="permission_id",referencedColumnName="id")
			)
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Permission> getPermissions() {
		// TODO: Ukloniti...
		return this.permissions;
	}
	
	/**
	 * Postavljanje dozvola.
	 * 
	 * @param permissions Nove dozvole.
	 */
	public void setPermissions(Set<Permission> permissions) {
		// TODO: Ukloniti...
		this.permissions = permissions;
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
	 * Pojedinosti o načinu objave aktivnosti.
	 * @return
	 */
	@OneToOne(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public UserActivityPrefs getUserActivityPrefs() {
		return userActivityPrefs;
	}
	public void setUserActivityPrefs(UserActivityPrefs userActivityPrefs) {
		this.userActivityPrefs = userActivityPrefs;
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
		if (!(obj instanceof UserDescriptor))
			return false;
		final UserDescriptor other = (UserDescriptor) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
