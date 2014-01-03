package hr.fer.zemris.jcms.model;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Odgovor na poruku/pitanje/problem/issue.
 * 
 * NAPOMENA: U kontekstu Ferkovog issue-tracking sustava, termini: poruka, pitanje, problem, issue i message su EKVIVALENTNI
 */

@Entity
@Table(name="issue_answers")
@NamedQueries({
    @NamedQuery(name="IssueAnswer.listForIssue",query="select m from IssueAnswer as m where m.parent.id=:issueID")
})
public class IssueAnswer {
	
	private Long id;
	private Issue parent;
	private String content;
	private User user;
	private Date date;
	
	/**
	 * Kada se odgovara na Issue (postavljeno pitanje), svaki novi odgovor
	 * dobiva referencu na pocetno postavljeno pitanje i postavlja se u listu djece
	 * tog pitanja. Tj. hijerarhija je max. dubine 1
	 * @param parent  vrsni roditelj tj. prvo postavljeno pitanje na koje se lijepe svi odgovori
	 * @return
	 */
	public IssueAnswer(Issue parent) {
		parent.addChild(this);
		this.parent = parent;
	}
	
	public IssueAnswer(){
		
	}
	
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Roditeljska/početna poruka
	 * @return
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public Issue getParent() {
		return parent;
	}
	public void setParent(Issue parent) {
		this.parent = parent;
	}

	/**
	 * Sadržaj odgovora
	 * @return
	 */
	@Column(length=1000,nullable=false)
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Vlasnik/autor odgovora
	 * @return
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDate() == null) ? 0 : getDate().hashCode());
		result = prime * result + ((getParent() == null) ? 0 : getParent().hashCode());
		result = prime * result + ((getUser() == null) ? 0 : getUser().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IssueAnswer))
			return false;
		IssueAnswer other = (IssueAnswer) obj;
		if (getDate() == null) {
			if (other.getDate() != null)
				return false;
		} else if (!getDate().equals(other.getDate()))
			return false;
		if (getParent() == null) {
			if (other.getParent() != null)
				return false;
		} else if (!getParent().equals(other.getParent()))
			return false;
		if (getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!getUser().equals(other.getUser()))
			return false;
		return true;
	}
}
