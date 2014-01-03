package hr.fer.zemris.jcms.model;


import hr.fer.zemris.jcms.model.extra.RepositoryFileStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceProperty;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


/**
 * Razred predstavlja jedan komentar na stranici. 
 * 
 */
@Entity
@Table(name="repository_file_page_comments")
public class RepositoryFilePageComment implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* Predlozeni model:
	 * FilePageComment
  		+- id
		+- filePage // kojoj stranici pripada
		+- comment
	 	+- user   //user koji je dao komentar
	 	+- date
	 */
	
	private Long id;
	private RepositoryFilePage repositoryFilePage;
	private String comment = ""; //sam komentar
	private User user;
	private Date date;
	private int commentType; //kakav je tip komentara
	
	
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
	public RepositoryFilePage getRepositoryFilePage() {
		return repositoryFilePage;
	}
	public void setRepositoryFilePage(RepositoryFilePage repositoryFilePage) {
		this.repositoryFilePage = repositoryFilePage;
	}
	
	//nullable false ili true?
	@Column(length = 5000, nullable = false)
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Column(nullable = false)
	public int getCommentType() {
		return commentType;
	}
	
	public void setCommentType(int commentType) {
		this.commentType = commentType;
	}
	
	
	
	
}
