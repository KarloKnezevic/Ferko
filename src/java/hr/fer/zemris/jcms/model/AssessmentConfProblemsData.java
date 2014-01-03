package hr.fer.zemris.jcms.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Rezultati studenta na provjeri znanja.
 * 
 * @author Ivan Krišto
 */
@NamedQueries({
    @NamedQuery(name="AssessmentConfProblemsData.listConfProblemsDataForAssessement",query="select acpd from AssessmentConfProblemsData as acpd where acpd.assessmentConfProblems=:assessmentConfProblems"),
    @NamedQuery(name="AssessmentConfProblemsData.getConfProblemsDataForAssessementAndUserId",query="select acpd from AssessmentConfProblemsData as acpd where acpd.assessmentConfProblems=:assessmentConfProblems and user.id=:id")
})
@Entity
@Table(name="assessmentsConfProblemsData", uniqueConstraints={
	@UniqueConstraint(columnNames={"assessmentConfProblems_id", "user_id"})
})
public class AssessmentConfProblemsData {
	
	/** Identifikator. */
	private Long id;
	
	/** Korisnik čiji su podatci ovdje. */
	private User user;
	
	/** Korisnik koji je postavio podatke ovdje. */
	private User assigner;
	
	/** Opisnik provjere uz koju su ovi podatci vezani. */
	private AssessmentConfProblems assessmentConfProblems;
	
	/** Grupa studenta. */
	private String group;
	
	/** Polje koje čuva sve bodove provjere studenta. */
	private byte[] score;
	
	/** Je li student pristupio ovoj provjeri. */
	private boolean present;
	
	/** Deserijalizirani niz rezultata provjera. */
	private transient Double[] dscore;
	
	/** Jesu li rezultati deserijalizirani? */
	private transient boolean opened = false;
	
	/** Jesu li rezultati mijenjani? */
	private transient boolean modified = false;

	/** Kada modificiramo bodove, score cemo preusmjeriti na ovo, kako bi hibernate dozivio objekt kao dirty, i pokrenuo snimanje. **/
	private static final byte[] dummy = new byte[0];
	
	private long version;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentConfProblemsData() {
	}
	
	/**
	 * Konstruktor provjere sa rezultatima postavljenim na nule.
	 * 
	 * @param numberOfProblems Broj zadataka od kojih se sastoji provjera.
	 */
	public AssessmentConfProblemsData(int numberOfProblems) {
		this.modified = true;
		this.opened = true;
		this.dscore = new Double[numberOfProblems];
	}

	@Version
	@Column(name="OPTLOCK")
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * Dohvat pojedinačnog rezultata provjere.
	 * @param index Index rezultata.
	 * @return Ostvareni bodovi.
	 */
	@Transient
	public Double getScoreFor(int index) {
		if(!this.opened) {
			open();
		}
		
		return dscore[index];
	}
	
	/**
	 * Postavljanje pojedinačnog rezultata provjere.
	 * 
	 * @param index Index rezultata.
	 * @param value Ostvareni bodovi.
	 */
	@Transient
	public void setScoreFor(int index, Double value) {
		if(!this.opened) {
			open();
		}
		
		// Ako su stara i nova vrijednost jednake, izađi van.
		// Inače postavi vrijednost i zastavicu modified.
		if (this.dscore[index] == null) {
			if (value == null) {
				return;
				
			} else {
				this.dscore[index] = value;
			    markModified();
			    
			}
		} else if (this.dscore[index].equals(value)) {
			return;
			
		} else {
			this.dscore[index] = value;
		    markModified();
		}
	}
	
	/**
	 * Postavljanje deserijaliziranog rezultata.
	 */
	@Transient
	private void open() {
		if (this.score == null) {
			this.dscore =  null;
			this.opened = true;
			
			return;
		}
		
		ByteArrayInputStream bis = new ByteArrayInputStream(this.score);
		try {
			ObjectInputStream ois = new ObjectInputStream(bis);
			this.dscore = (Double[]) ois.readObject();
			ois.close();
		} catch (IOException e) {}
		  catch (ClassNotFoundException e) {}
		
	    this.opened = true;
	}
	
	/**
	 * Zapis serijaliziranog rezultata provjera u bazu.
	 */
	@Transient
	@PreUpdate @PrePersist
	@SuppressWarnings("unused")
	private void write() {
		if(!this.modified) {
			return;
		}
		
		if (this.dscore == null) {
			setScore(null);
			return;
		}
		
	    // Serijaliziraj dscore
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this.dscore);
			oos.close();
		} catch (IOException e) {}
		
	    setScore(bos.toByteArray());
	}
	
	/**
	 * @return Id.
	 */
	@Id @GeneratedValue
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * @return Opisnik provjere uz koju su ovi podatci vezani.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public AssessmentConfProblems getAssessmentConfProblems() {
		return this.assessmentConfProblems;
	}

	public void setAssessmentConfProblems(AssessmentConfProblems assessmentConfProblems) {
		this.assessmentConfProblems = assessmentConfProblems;
	}
	
	/**
	 * @return Grupa studenta ("putanja").
	 */
	@Column(name="assessmentGroup", length=15, nullable=true)
	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	/**
	 * @return Je li student pristupio ovoj provjeri.
	 */
	@Column(nullable=false)
	public boolean getPresent() {
		return this.present;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}
	
	/**
	 * @return Polje koje čuva sve bodove provjere studenta.
	 */
	@Lob
	@Column(length=500)
	public byte[] getScore() {
		return this.score;
	}

	public void setScore(byte[] score) {
		this.score = score;
		this.modified = false;
	}
	
	/**
	 * @return Korisnik čiji su podatci ovdje.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return Korisnik koji je postavio podatke ovdje.
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getAssigner() {
		return this.assigner;
	}

	public void setAssigner(User assigner) {
		this.assigner = assigner;
	}
	
	/**
	 * @return Deserijalizirani niz rezultata provjera.
	 */
	@Transient
	public Double[] getDscore() {
		if (!this.opened) {
			open();
		}
		
		return this.dscore;
	}

	// Komentirano radi debugiranja (Ivan Krišto)
	// TODO: Koristiti ovu metodu umjesto one druge setDscore()
//	@Transient
//	public void setDscore(Double[] dscore) {
//		if (dscore == null) {
//			if (this.dscore == null) {
//				return;
//				
//			} else {
//				this.modified = true;
//				this.opened = true;
//				this.dscore = null;
//				return;
//				
//			}
//		}
//		if ((this.dscore != null) && this.opened && !this.modified) {
//			// Ako su rezultati već otvoreni a nisu prethodno mijenjani,
//			// provjeri jesu li ovom akcijom promjenjeni.
//			for (int i = 0; i < dscore.length; i++) {
//				if (this.dscore[i] == null) {
//					if (dscore[i] != null) {
//						this.modified = true;
//						this.opened = true;
//						break;
//					}
//				} else {
//					if (dscore[i] == null) {
//						this.modified = true;
//						this.opened = true;
//						break;
//					}
//					
//					double absDiff = Math.abs(dscore[i].doubleValue() - this.dscore[i].doubleValue());
//					if (absDiff > 1E-5) {
//						this.modified = true;
//						this.opened = true;
//						break;
//					}
//				}
//			}
//			
//		} else {
//			// Inače ih postavi kao mijenjane i otvorene.
//			this.modified = true;
//			this.opened = true;
//		}
//		
//		this.dscore = dscore;
//	}
	
	@Transient
	public void setDscore(Double[] dscore) {
	    markModified();
		this.opened = true;
		
		this.dscore = dscore;
	}
	
	private void markModified() {
		if(modified) return;
		modified = true;
		score = dummy; // nesto smo promijenili - objekt ce se azurirati
		// ovime naoko gubimo podatke, ali to nije istina; ako je modified=true, to znaci da su podaci
		// vec u polju Double[]-ova i njima cemo rekonstruirati score.
	}

	/**
	 * @return Je li podatak o broju bodova mijenjan.
	 */
	@Transient
	public boolean isModified() {
		return this.modified;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessmentConfProblems() == null) ? 0 : getAssessmentConfProblems().hashCode());
		result = prime * result
				+ ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AssessmentConfProblemsData)) {
			return false;
		}
		
		AssessmentConfProblemsData other = (AssessmentConfProblemsData) obj;
		if (getAssessmentConfProblems() == null) {
			if (other.getAssessmentConfProblems() != null) {
				return false;
			}
		} else if (!(getAssessmentConfProblems().equals(other.getAssessmentConfProblems()))) {
			return false;
		}
		if (getUser() == null) {
			if (other.getUser() != null) {
				return false;
			}
		} else if (!getUser().equals(other.getUser())) {
			return false;
		}
		
		return true;
	}
}
