package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.extra.IssueStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Konkretna poruka/pitanje/problem/issue
 * 
 * NAPOMENA: U kontekstu Ferkovog issue-tracking sustava termini: poruka, pitanje, problem, issue i message su EKVIVALENTNI
 */
 
@Entity
@Table(name="issues")
@NamedQueries({
    @NamedQuery(name="Issue.listForUser",query="select m from Issue as m where m.declaredPublic = true or m.user.username LIKE :username"),
    @NamedQuery(name="Issue.listActiveForAsistent",query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND m.status != 'RESOLVED'"),
    @NamedQuery(name="Issue.listActiveForAsistent2",query="select m from Issue as m JOIN m.versions as v where m.topic.courseInstance.id = :courseInstanceID AND  v.user.id=:userID AND (m.status != 'RESOLVED' OR m.modificationVersion > v.issueVersion)"),
    @NamedQuery(name="Issue.listResolvedForAsistent",query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND m.status = 'RESOLVED'"),
    @NamedQuery(name="Issue.listResolvedForAsistent2",query="select m from Issue as m JOIN m.versions as v where m.topic.courseInstance.id = :courseInstanceID AND  v.user.id=:userID AND m.status = 'RESOLVED' AND m.modificationVersion = v.issueVersion)"),
    @NamedQuery(name="Issue.listActiveForStudent",query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND ((m.declaredPublic = true and m.status!= 'RESOLVED') OR (m.user.id=:userID AND (m.status!='RESOLVED' OR m.modificationVersion>m.studentVersion)))"),
    @NamedQuery(name="Issue.listResolvedForStudent",query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND ((m.declaredPublic = true and m.status = 'RESOLVED') OR (m.user.id=:userID AND m.status ='RESOLVED' AND m.studentVersion = m.modificationVersion))"),
  //@NamedQuery(name="Issue.updateCheckStaff", query="select count(*) from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND m.modificationVersion > m.asistentVersion"),
    @NamedQuery(name="Issue.updateCheckStudent", query="select count(*) from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND m.user.id = :ownerID AND m.modificationVersion > m.studentVersion"),
    @NamedQuery(name="Issue.getByID",query="select m from Issue as m where m.id=:messageID"),
    @NamedQuery(name="Issue.listPostponedForActivation",query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID AND m.status = 'POSTPONED' AND now() >= delayDate"),
    @NamedQuery(name="Issue.updateCheckStaff2", query="select count(*) from Issue as m JOIN m.versions as v where m.topic.courseInstance.id = :courseInstanceID AND m.modificationVersion > v.issueVersion AND v.user.id=:userID"),
    @NamedQuery(name="Issue.listAssistantsWithVersions", query="select distinct v.user.id from Issue as m JOIN m.versions as v where m.topic.courseInstance.id = :courseInstanceID"),
    @NamedQuery(name="Issue.listIssuesOnCourse", query="select m from Issue as m where m.topic.courseInstance.id = :courseInstanceID")
}	
)
public class Issue implements Comparable<Issue>{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String content;
	private User user;
	private IssueTopic topic;
	private IssueStatus status;
	private Date delayDate; 
	private List<IssueAnswer> children = new ArrayList<IssueAnswer>();
	private Date creationDate;   
	private Date lastModificationDate;
	private boolean declaredPublic = false;
	private Set<IssueAssistantVersion> versions = new HashSet<IssueAssistantVersion>();
	
	
	//verzija na našu kontrolu, inicijalno 0, kod bilo kakve promjene asistenta poveća se za 1, 
	//		kad god stigne odgovor bilo koga poveća se za 1 i izjednače se verzije
	//kad student/asistent fizički pročita (klikne na new message), ažurira se njegov version na aktualni modification version
	//smjer prijenosa vrijednosti je samo iz modification version u asistent/student version
	//modification version se poveća kod svake izmjene i prebaci se u pripadni asistent/student version
	private int modificationVersion;  //bilo koja izmjena u podacima ovog razreda ili razreda djece
	private int studentVersion;
	
	private int version;

	
	public Issue() {
		this.delayDate=null;
		this.setStudentVersion(0);
		this.setModificationVersion(0);
	}
	
	/**
	 * Identifikator poruke
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
	 * Naslov poruke
	 * @return
	 */
	@Column(length=100,nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sadržaj poruke
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
	@ManyToOne(fetch=FetchType.EAGER)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public IssueTopic getTopic() {
		return topic;
	}
	public void setTopic(IssueTopic topic) {
		this.topic = topic;
	}

	/**
	 * Status poruke
	 * @return
	 */
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public IssueStatus getStatus() {
		return status;
	}
	
	public void setStatus(IssueStatus status) {
		this.status = status;
	}
	
	/**
	 * Vrijeme odgode poruke, 
	 * ako je status poruke postavljen kao POSTPONED.
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDelayDate() {
		return delayDate;
	}
	public void setDelayDate(Date delayDate) {
		this.delayDate = delayDate;
	}
	
	/**
	 * Lista odgovora
	 * @return
	 */
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public List<IssueAnswer> getChildren() {
		return children;
	}
	public void setChildren(List<IssueAnswer> children) {
		this.children = children;
	}
	public void addChild(IssueAnswer child) {
		this.getChildren().add(child);
	}

	/**
	 * Vrijeme i datum slanja poruke.
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}
	
	/**
	 * Vrijeme i datum zadnje izmjene poruke
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getLastModificationDate() {
		return lastModificationDate;
	}
	public void setLastModificationDate(Date date) {
		this.lastModificationDate = date;
	}
	
	@OneToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.REMOVE},mappedBy="issue")
	@Fetch(FetchMode.SELECT)
	public Set<IssueAssistantVersion> getVersions() {
		return versions;
	}
	public void setVersions(Set<IssueAssistantVersion> versions) {
		this.versions = versions;
	}
	
	/**
	 * Zastavica koja oznacava zelimo li da poruku vide svi useri
	 * pretplaceni na temu poruke, npr. ako je neko common pitanje ili slicno.
	 * Postavlja ju asistent, NE student.
	 * @return
	 */
	public boolean isDeclaredPublic() {
		return declaredPublic;
	}
	public void setDeclaredPublic(boolean declaredPublic) {
		this.declaredPublic = declaredPublic;
	}
	

	public int getModificationVersion() {
		return modificationVersion;
	}
	public void setModificationVersion(int modificationVersion) {
		this.modificationVersion = modificationVersion;
	}

	public int getStudentVersion() {
		return studentVersion;
	}
	public void setStudentVersion(int studentVersion) {
		this.studentVersion = studentVersion;
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
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Issue))
			return false;
		Issue other = (Issue) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public int compareTo(Issue o) {
		//Sortiranje primarno po statusu, a potom po datumu stvaranja
		if(this.getStatus().equals(o.getStatus())){
			return -1 * this.getLastModificationDate().compareTo(o.getLastModificationDate());
		}
		else return this.getStatus().compareTo(o.getStatus());
	}


}
