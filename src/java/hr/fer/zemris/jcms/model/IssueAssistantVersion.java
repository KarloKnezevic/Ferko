package hr.fer.zemris.jcms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Personalizirane verzije pojedinih pitanja za osoblje kolegija
 * 
 * NAPOMENA: U kontekstu Ferkovog issue-tracking sustava termini: poruka, pitanje, problem, issue i message su EKVIVALENTNI
 */

@Entity
@Table(name="issue_assistant_versions", uniqueConstraints={
		// Ne mogu postojati dva retka s jednakim vrijednostima para Issue-User
		@UniqueConstraint(columnNames={"issue_id","user_id"})
})
public class IssueAssistantVersion {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Issue issue;
	private User user;
	private int issueVersion;
	//optimistic lock support
	private int version;

	
	public IssueAssistantVersion() {

	}
	
	public IssueAssistantVersion(Issue issue, User u, int ver) {
		this.issue = issue;
		this.user=u;
		this.issueVersion = ver;
	}
	
	/**
	 * Identifikator
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
	 * Vlasnik/autor poruke
	 * @return
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Tema poruke. 
	 * Svaka poruka je uvijek vezana uz neku temu (IssueTopic)
	 * Svaki kolegij ima ponuđen skup IssueTopica
	 * @return
	 */
	@OnDelete(action=OnDeleteAction.CASCADE)
	@ManyToOne
	@JoinColumn(name="issue_id")
	public Issue getIssue() {
		return issue;
	}
	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	/**
	 * Zadnja aktualna verzija issuea za trenutnog korisnika
	 * @param assistantVersion
	 */
	@Column(nullable=false)
	public void setIssueVersion(int issueVersion) {
		this.issueVersion = issueVersion;
	}

	public int getIssueVersion() {
		return issueVersion;
	}

	/**
	 * Version. Optimistic lock support.
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
		result = prime * result + ((getUser().getId() == null) ? 0 : getUser().hashCode());
		result = prime * result + ((getIssue().getId() == null) ? 0 : getIssue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IssueAssistantVersion))
			return false;
		IssueAssistantVersion other = (IssueAssistantVersion) obj;
		if (getUser().getId() == null) {
			if (other.getUser().getId() != null)
				return false;
		} else if (!getUser().getId().equals(other.getUser().getId()))
			return false;
		if (getIssue().getId() == null) {
			if (other.getIssue().getId() != null)
				return false;
		} else if (!getIssue().getId().equals(other.getIssue().getId()))
			return false;		
		return true;
	}

}
