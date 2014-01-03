package hr.fer.zemris.jcms.model;


import hr.fer.zemris.jcms.model.extra.RepositoryFileStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


/**
 * Razred predstavlja jednu datoteku u repozitoriju. 
 * 
 * 
 */


@NamedQueries({
    @NamedQuery(name="RepositoryFile.findByOwner",query="select rfile from RepositoryFile as rfile where rfile.owner=:ownId")
    //QUERIES TODO
})
@Entity
@Table(name="repository_files")
public class RepositoryFile implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* Predlozeni model:
	 * File
  		+- id
  		+- realName: ovo je ime kako je uploadano
  		+- pointer na repozitorij
 		+- pointer na file koji predstavlja prethodnu verziju?  - Da li je potrebno ovako ili nekako drugacije?
  		+- verzija (automatski!)
  		+- status - moze biti hidden ili visible
  		+- datumUploada (datum zadnje izmjene)
  		+- kategorija u repozitoriju
  		+- user (vlasnik)
  	 *	
  	 *	+++ dodatno:  Opis datoteke koju unosi uploader (komentar uz datoteku)
     *  
     *  //izbaceno-- path: gdje je na disku repozitorij + neko random ime ili cak ime=id?
	 */
	
	private Long id;
	private String realName;
	private RepositoryCourse repositoryCourse; 
	private RepositoryFileStatus status;
	private Date uploadDate;
	private String comment;
	private RepositoryCategory category; 
	private User owner;
	private int fileVersion;
	private String mimeType;
	private RepositoryFile nextVersion;
	private RepositoryFile previousVersion;
	private List<RepositoryFilePage> filePages = new ArrayList<RepositoryFilePage>();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public RepositoryCourse getRepositoryCourse() {
		return repositoryCourse;
	}

	public void setRepositoryCourse(RepositoryCourse repositoryCourse) {
		this.repositoryCourse = repositoryCourse;
	}
	
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	@Column(length = 100, nullable = false)
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	@Enumerated(EnumType.STRING)
	public RepositoryFileStatus getStatus() {
		return status;
	}
	public void setStatus(RepositoryFileStatus status) {
		this.status = status;
	}
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	
	@Column(length = 150, nullable = true)
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
    
	@ManyToOne
	public RepositoryCategory getCategory() {
		return category;
	}
	
	public void setCategory(RepositoryCategory category) {
		this.category = category;
	}
	
	@Column(nullable=false)
	public int getFileVersion() {
		return fileVersion;
	}
	
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}
	
	@Column(length = 50, nullable = true)
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public RepositoryFile getNextVersion() {
		return nextVersion;
	}

	public void setNextVersion(RepositoryFile nextVersion) {
		this.nextVersion = nextVersion;
	}
	
	
	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public RepositoryFile getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(RepositoryFile previousVersion) {
		this.previousVersion = previousVersion;
	}

	
	@OneToMany(fetch=FetchType.EAGER,mappedBy="repositoryFile")
	@Fetch(FetchMode.SELECT)
	public List<RepositoryFilePage> getFilePages() {
		return filePages;
	}

	public void setFilePages(List<RepositoryFilePage> filePages) {
		this.filePages = filePages;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getCategory() == null) ? 0 : getCategory().hashCode());
		result = prime * result + getFileVersion();
		result = prime * result
				+ ((getRealName() == null) ? 0 : getRealName().hashCode());
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
		if (!(obj instanceof RepositoryFile))
			return false;
		RepositoryFile other = (RepositoryFile) obj;
		if (getCategory() == null) {
			if (other.getCategory() != null)
				return false;
		} else if (!getCategory().equals(other.getCategory()))
			return false;
		if (getFileVersion() != other.getFileVersion())
			return false;
		if (getRealName() == null) {
			if (other.getRealName() != null)
				return false;
		} else if (!getRealName().equals(other.getRealName()))
			return false;
		return true;
	}
	
	
	


	
}
