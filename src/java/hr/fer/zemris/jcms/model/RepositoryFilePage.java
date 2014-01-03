package hr.fer.zemris.jcms.model;


import hr.fer.zemris.jcms.model.extra.RepositoryFileStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


/**
 * Razred predstavlja jednu stranicu jednog dokumenta. 
 * 
 */
@Entity
@Table(name="repository_file_page", uniqueConstraints={
		// Ne mogu postojati dvije stranice iste datoteke
		@UniqueConstraint(columnNames={"repositoryFile_id","page"})
})
public class RepositoryFilePage implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* Predlozeni model:
	 * FilePage
  		+- id
		+- repositoryFile
	 	+- page
	 	+- rating
	 	+- comments
	 
	 */
	
	private Long id;
	private RepositoryFile repositoryFile;
	private int page;
	private List<RepositoryFilePageComment> comments = new ArrayList<RepositoryFilePageComment>();
	 
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/*
	 * 
	 */
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public RepositoryFile getRepositoryFile() {
		return repositoryFile;
	}
	public void setRepositoryFile(RepositoryFile repositoryFile) {
		this.repositoryFile = repositoryFile;
	}
	
	@Column(nullable = false)	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	
	@OneToMany(fetch=FetchType.EAGER,mappedBy="repositoryFilePage")
	@Fetch(FetchMode.SELECT)
	public List<RepositoryFilePageComment> getComments() {
		return comments;
	}
	public void setComments(List<RepositoryFilePageComment> comments) {
		this.comments = comments;
	}

	
	
}
