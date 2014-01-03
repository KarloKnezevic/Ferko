package hr.fer.zemris.jcms.model.appeals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Fizički model žalbe za provjere znanja.
 * 
 * @author Ivan Krišto
 */
@Entity
@Table(name="assessment_appeal_instance",uniqueConstraints=@UniqueConstraint(
		columnNames={"creationDate","creatorUser_id","assessment_id"}
))
@NamedQueries({
    @NamedQuery(name="AssessmentAppealInstance.listAppealsForAssessment",query="select a from AssessmentAppealInstance as a where a.assessment=:assessment"),
    @NamedQuery(name="AssessmentAppealInstance.getAppealForId",query="select a from AssessmentAppealInstance as a where a.id=:id"),
    @NamedQuery(name="AssessmentAppealInstance.listAppealsForCourse",query="select a from AssessmentAppealInstance as a where a.assessment.courseInstance=:course"),
    @NamedQuery(name="AssessmentAppealInstance.listAppealsForUserAndAssessment",query="select a from AssessmentAppealInstance as a where a.assessment=:assessment and a.creatorUser=:user")
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AssessmentAppealInstance {
	
	/** ID žalbe. */
	private Long id;
	
	/** Datum stvaranja. */
	private Date creationDate;
	
	/** Datum zadnje izmjene. */
	private Date lastModified;
	
	/** Osoba koja se žali. */
	private User creatorUser;
	
	/** Osoba koja je zaključala žalbu. */
	private User lockerUser;
	
	/** Osoba koja je obradila žalbu. */
	private User solverUser;
	
	/** Stanje žalbe. */
	private AppealInstanceStatus status;
	
	/** Provjera na koju se žalba odnosi. */
	private Assessment assessment;
	
	/** Lista vrijednosti osobina koje problem sadrži. */
	private List<String> propertiesValues;
	
	/** Lista naziva osobina (ključeva) koje problem sadrži. */
	private List<String> propertiesKeys;
	
	/** Lista tipova podataka osobina koje problem sadrži. */
	private List<Class<?>> propertiesType;
	
	/** Komentar na problem. */
	private String comment;
	
	/** Tip problema. */
	private AppealProblemType type;
	
	/** Verzija. */
	private int version;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentAppealInstance () {
	}
	
	/**
	 * Privatni konstruktor.
	 * 
	 * @param assessment Provjera na koju se žalba odnosi.
	 * @param creatorUser Korisnik koji je stvorio žalbu.
	 */
	private AssessmentAppealInstance(Assessment assessment, User creatorUser, AppealProblemType type, String comment) {
		this.assessment = assessment;
		this.creatorUser = creatorUser;
		this.solverUser = null;
		this.lockerUser = null;
		this.status = AppealInstanceStatus.OPENED;
		this.creationDate = new Date();
		this.lastModified = new Date();
		this.type = type;
		if (comment == null) {
			this.comment = "";
		} else {
			this.comment = comment;
		}
	}
	
	/**
	 * Stvaranje nove žalbe.
	 * 
	 * @param assessment Provjera na koju se žalba odnosi.
	 * @param creatorUser Korisnik koji je stvorio žalbu.
	 * @return Instanca žalbe.
	 */
	@Transient
	public static AssessmentAppealInstance createAppeal(Assessment assessment, User creatorUser, AppealProblemType type, String comment) {
		return new AssessmentAppealInstance(assessment, creatorUser, type, comment);
	}
	
	/**
	 * Dohvat vrijednosti integer parametra na osnovu naziva.
	 * 
	 * @param key Naziv parametra.
	 * @return Vrijednost parametra.
	 */
	@Transient
	public int getIntProperty(String key) {
		int index = getPropertiesKeys().indexOf(key);
		
		if (!getPropertiesType().get(index).equals(Integer.class)) {
			throw new IllegalArgumentException("Vrijednost pod ključem '" + key + "' nije tipa int!");
		}
		
		if (index == -1) {
			throw new IndexOutOfBoundsException("Vrijednost pod ključem '" + key + "' ne postoji!");
			
		} else {
			int val = Integer.parseInt(getPropertiesValues().get(index));
			return val;
		}
	}
	
	/**
	 * Dohvat vrijednosti double parametra na osnovu naziva.
	 * 
	 * @param key Naziv parametra.
	 * @return Vrijednost parametra.
	 */
	@Transient
	public double getDoubleProperty(String key) {
		int index = getPropertiesKeys().indexOf(key);
		
		if (!getPropertiesType().get(index).equals(Double.class)) {
			throw new IllegalArgumentException("Vrijednost pod ključem '" + key + "' nije tipa double!");
		}
		
		if (index == -1) {
			throw new IndexOutOfBoundsException("Vrijednost pod ključem '" + key + "' ne postoji!");
			
		} else {
			double val = Double.parseDouble(getPropertiesValues().get(index));
			return val;
		}
	}
	
	/**
	 * Dohvat vrijednosti boolean parametra na osnovu naziva.
	 * 
	 * @param key Naziv parametra.
	 * @return Vrijednost parametra.
	 */
	@Transient
	public boolean getBooleanProperty(String key) {
		int index = getPropertiesKeys().indexOf(key);
		
		if (!getPropertiesType().get(index).equals(Boolean.class)) {
			throw new IllegalArgumentException("Vrijednost pod ključem '" + key + "' nije tipa boolean!");
		}
		
		if (index == -1) {
			throw new IndexOutOfBoundsException("Vrijednost pod ključem '" + key + "' ne postoji!");
			
		} else {
			boolean val = Boolean.parseBoolean(getPropertiesValues().get(index));
			return val;
		}
	}
	
	/**
	 * Dohvat vrijednosti String parametra na osnovu naziva.
	 * 
	 * @param key Naziv parametra.
	 * @return Vrijednost parametra.
	 */
	@Transient
	public String getStringProperty(String key) {
		int index = getPropertiesKeys().indexOf(key);
		
		if (!getPropertiesType().get(index).equals(String.class)) {
			throw new IllegalArgumentException("Vrijednost pod ključem '" + key + "' nije tipa String!");
		}
		
		if (index == -1) {
			throw new IndexOutOfBoundsException("Vrijednost pod ključem '" + key + "' ne postoji!");
			
		} else {
			String val = getPropertiesValues().get(index);
			return val;
		}
	}
	
	/**
	 * Dohvat tipa parametra na osnovu naziva.
	 * 
	 * @param key Naziv parametra.
	 * @return Tip parametra.
	 */
	@Transient
	public Class<?> getPropertyType(String key) {
		int index = getPropertiesKeys().indexOf(key);
		
		if (index == -1) {
			return null;
			
		} else {
			return getPropertiesType().get(index);
			
		}
	}
	
	/**
	 * Dodavanje novog parametra tipa String.
	 * 
	 * @param propertyName Naziv parametra.
	 * @param propertyValue Vrijednost parametra.
	 * @return Objekt kojem je parametar dodan.
	 */
	@Transient
	public AssessmentAppealInstance addProperty(String propertyName, String propertyValue) {
		if (getPropertiesKeys() == null) {
			setPropertiesKeys(new ArrayList<String>());
		}
		if (getPropertiesType() == null) {
			setPropertiesType(new ArrayList<Class<?>>());
		}
		if (getPropertiesValues() == null) {
			setPropertiesValues(new ArrayList<String>());
		}
		
		getPropertiesKeys().add(propertyName);
		getPropertiesType().add(String.class);
		getPropertiesValues().add(propertyValue);
		
		return this;
	}
	
	/**
	 * Dodavanje novog parametra tipa int.
	 * 
	 * @param propertyName Naziv parametra.
	 * @param propertyValue Vrijednost parametra.
	 * @return Objekt kojem je parametar dodan.
	 */
	@Transient
	public AssessmentAppealInstance addProperty(String propertyName, int propertyValue) {
		if (getPropertiesKeys() == null) {
			setPropertiesKeys(new ArrayList<String>());
		}
		if (getPropertiesType() == null) {
			setPropertiesType(new ArrayList<Class<?>>());
		}
		if (getPropertiesValues() == null) {
			setPropertiesValues(new ArrayList<String>());
		}
		
		getPropertiesKeys().add(propertyName);
		getPropertiesType().add(Integer.class);
		getPropertiesValues().add(Integer.toString(propertyValue));
		
		return this;
	}
	
	/**
	 * Dodavanje novog parametra tipa double.
	 * 
	 * @param propertyName Naziv parametra.
	 * @param propertyValue Vrijednost parametra. 
	 * @return Objekt kojem je parametar dodan.
	 */
	@Transient
	public AssessmentAppealInstance addProperty(String propertyName, double propertyValue) {
		if (getPropertiesKeys() == null) {
			setPropertiesKeys(new ArrayList<String>());
		}
		if (getPropertiesType() == null) {
			setPropertiesType(new ArrayList<Class<?>>());
		}
		if (getPropertiesValues() == null) {
			setPropertiesValues(new ArrayList<String>());
		}
		
		getPropertiesKeys().add(propertyName);
		getPropertiesType().add(Double.class);
		getPropertiesValues().add(Double.toString(propertyValue));
		
		return this;
	}
	
	/**
	 * Dodavanje novog parametra tipa boolean.
	 * 
	 * @param propertyName Naziv parametra.
	 * @param propertyValue Vrijednost parametra. 
	 * @return Objekt kojem je parametar dodan.
	 */
	@Transient
	public AssessmentAppealInstance addProperty(String propertyName, boolean propertyValue) {
		if (getPropertiesKeys() == null) {
			setPropertiesKeys(new ArrayList<String>());
		}
		if (getPropertiesType() == null) {
			setPropertiesType(new ArrayList<Class<?>>());
		}
		if (getPropertiesValues() == null) {
			setPropertiesValues(new ArrayList<String>());
		}
		
		getPropertiesKeys().add(propertyName);
		getPropertiesType().add(Boolean.class);
		getPropertiesValues().add(Boolean.toString(propertyValue));
		
		return this;
	}
	
	/**
	 * @return the id
	 */
	@Id @GeneratedValue
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Datum stvaranja.
	 * 
	 * @return the creationDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Datum zadnje promjene.
	 * 
	 * @return the lastModified
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastModified() {
		return this.lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Provjera na koju se žalba odnosi.
	 * 
	 * @return the assessment
	 */
	@ManyToOne(fetch=FetchType.EAGER, cascade=CascadeType.REMOVE)
	public Assessment getAssessment() {
		return this.assessment;
	}

	/**
	 * @param assessment the assessment to set
	 */
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	/**
	 * Korisnik koji je stvorio žalbu.
	 * 
	 * @return the creatorUser
	 */
	@ManyToOne(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
	public User getCreatorUser() {
		return this.creatorUser;
	}

	/**
	 * @param creatorUser the creatorUser to set
	 */
	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

	/**
	 * Korisnik koji je zaključao žalbu.
	 * 
	 * @return the lockerUser
	 */
	@ManyToOne(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
	public User getLockerUser() {
		return this.lockerUser;
	}

	/**
	 * @param lockerUser the lockerUser to set
	 */
	public void setLockerUser(User lockerUser) {
		this.lockerUser = lockerUser;
	}

	/**
	 * Korisnik koji je obradio žalbu.
	 * 
	 * @return the solverUser
	 */
	@ManyToOne(fetch=FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
	public User getSolverUser() {
		return this.solverUser;
	}

	/**
	 * @param solverUser the solverUser to set
	 */
	public void setSolverUser(User solverUser) {
		this.solverUser = solverUser;
	}

	/**
	 * Stanje žalbe.
	 * 
	 * @return the status
	 */
	@Enumerated
	public AppealInstanceStatus getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(AppealInstanceStatus status) {
		this.status = status;
		setLastModified(new Date());
	}
	
	/**
	 * @return the type
	 */
	@Enumerated
	public AppealProblemType getType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(AppealProblemType type) {
		this.type = type;
	}

	/**
	 * Komentar na žalbu.
	 * 
	 * @return the comment
	 */
	public static final int COMMENT_LENGTH = 200;
	@Column(length=COMMENT_LENGTH,nullable=false)
	public String getComment() {
		return this.comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the propertiesKeys
	 */
	@CollectionOfElements
	public List<String> getPropertiesKeys() {
		if (this.propertiesKeys != null) {
			this.propertiesKeys.size(); // "Hack" radi inicijalizacije.
		}
		return this.propertiesKeys;
	}

	/**
	 * @param propertiesKeys the propertiesKeys to set
	 */
	public void setPropertiesKeys(List<String> propertiesKeys) {
		this.propertiesKeys = propertiesKeys;
	}

	/**
	 * @return the propertiesType
	 */
	@CollectionOfElements
	public List<Class<?>> getPropertiesType() {
		if (this.propertiesType != null) {
			this.propertiesType.size(); // "Hack" radi inicijalizacije.
		}
		return this.propertiesType;
	}

	/**
	 * @param propertiesType the propertiesType to set
	 */
	public void setPropertiesType(List<Class<?>> propertiesType) {
		this.propertiesType = propertiesType;
	}

	/**
	 * @return the propertiesValues
	 */
	@CollectionOfElements
	public List<String> getPropertiesValues() {
		if (this.propertiesValues != null) {
			this.propertiesValues.size(); // "Hack" radi inicijalizacije
		}
		return this.propertiesValues;
	}

	/**
	 * @param propertiesValues the propertiesValues to set
	 */
	public void setPropertiesValues(List<String> propertiesValues) {
		this.propertiesValues = propertiesValues;
	}
	
	/**
	 * Verzija. Podrška za optimistično zaključavanje.
	 * 
	 * @return the version
	 */
	@Version
	public int getVersion() {
		return this.version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((this.assessment == null) ? 0 : this.assessment.hashCode());
		result = PRIME * result + ((this.creationDate == null) ? 0 : this.creationDate.hashCode());
		result = PRIME * result + ((this.creatorUser == null) ? 0 : this.creatorUser.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AssessmentAppealInstance other = (AssessmentAppealInstance) obj;
		if (this.assessment == null) {
			if (other.assessment != null)
				return false;
		} else if (!this.assessment.equals(other.assessment))
			return false;
		if (this.creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!this.creationDate.equals(other.creationDate))
			return false;
		if (this.creatorUser == null) {
			if (other.creatorUser != null)
				return false;
		} else if (!this.creatorUser.equals(other.creatorUser))
			return false;
		return true;
	}

}
