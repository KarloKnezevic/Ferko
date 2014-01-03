package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Objekt koji čuva jednu uploadanu datoteku za nekog studenta na nekom
 * ispitu.
 * 
 * Definirano je unique ogranicenje: user+assessment+descriptor.
 * 
 * User moze biti null. Naime, na provjeru se mogu uploadati i datoteke koje nisu vezane za same korisnike, vec su vezane
 * isključivo za provjeru (primjerice, tekstovi zadataka i sl).
 * 
 * Pravo ime datoteke koju je imala prilikom uploada se ne cuva u bazi.
 *  
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="AssessmentFile.listReallyAllForAssessment",query="select a from AssessmentFile as a where a.assessment=:assessment"),
    @NamedQuery(name="AssessmentFile.listAllForAssessment",query="select a from AssessmentFile as a where a.assessment=:assessment and a.user is not null"),
    @NamedQuery(name="AssessmentFile.listAllForAssessmentUser",query="select a from AssessmentFile as a where a.assessment=:assessment and a.user=:user"),
    @NamedQuery(name="AssessmentFile.listAllForAssessmentOnly",query="select a from AssessmentFile as a where a.assessment=:assessment and a.user is null")
})
@Entity
@Table(name="assessment_files", uniqueConstraints=@UniqueConstraint(columnNames={"assessment_id","user_id","descriptor"}))
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AssessmentFile implements Comparable<AssessmentFile> {
	private Long id; // primarni kljuc
	private User user; // Korisnik čija je ovo datoteka
	private Assessment assessment; // ispit kojem datoteka pripada
	private String descriptor; // opisnik datoteke
	private String mimeType; // mime tip datoteke
	private String extension; // ekstenzija koju je datoteka imala prilikom uploada
	private String originalFileName;
	private String description;
	
	public AssessmentFile() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=true)
	@Fetch(FetchMode.SELECT)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	@Fetch(FetchMode.SELECT)
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	@Column(nullable=false, length=50)
	public String getDescriptor() {
		return descriptor;
	}
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	@Column(nullable=true, length=50)
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Column(nullable=true, length=10)
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}

	@Column(nullable=false, length=40)
	public String getOriginalFileName() {
		return originalFileName;
	}
	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}
	
	@Column(nullable=true, length=50)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getAssessment() == null) ? 0 : getAssessment().hashCode());
		result = prime * result
				+ ((getDescriptor() == null) ? 0 : getDescriptor().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AssessmentFile))
			return false;
		AssessmentFile other = (AssessmentFile) obj;
		if (getAssessment() == null) {
			if (other.getAssessment() != null)
				return false;
		} else if (!getAssessment().equals(other.getAssessment()))
			return false;
		if (getDescriptor() == null) {
			if (other.getDescriptor() != null)
				return false;
		} else if (!getDescriptor().equals(other.getDescriptor()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(AssessmentFile o) {
		if(this.getUser()==null) {
			if(o.getUser()==null) {
				if(this.getDescriptor()==null) {
					if(o.getDescriptor()==null) return 0;
					return -1;
				}
				return this.getDescriptor().compareTo(o.getDescriptor());
			}
			return -1;
		}
		if(o.getUser()==null) {
			return 1;
		}
		// Inace oba imaju korisnika (i po pretpostavci, taj je isti)
		if(this.getDescriptor()==null) {
			if(o.getDescriptor()==null) return 0;
			return -1;
		}
		return this.getDescriptor().compareTo(o.getDescriptor());
	}
}
