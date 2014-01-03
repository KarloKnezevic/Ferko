package hr.fer.zemris.jcms.model.poll;


import hr.fer.zemris.jcms.model.User;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@NamedQueries({
    @NamedQuery(name="Poll.all",query="select p from Poll as p"),
    @NamedQuery(name="Poll.byId",query="SELECT p FROM Poll p WHERE id=:id"),
    @NamedQuery(name="Poll.byOwnerId",query="select p from Poll p join p.owners u where u.id=:userId"),
    @NamedQuery(name="Poll.forOwnerAndPoll",query="select p from Poll p join p.owners u where u.id=:userId and p.id=:pollId and CURRENT_TIMESTAMP BETWEEN startDate and endDate"),
    @NamedQuery(name="Poll.byName",query="select p from Poll as p where title=:name"),  
    @NamedQuery(name="Poll.active",query="select p from Poll p where CURRENT_DATE BETWEEN startDate and endDate"),
    @NamedQuery(name="Poll.toAnswer",query="SELECT pu.poll FROM PollUser pu JOIN pu.poll p WHERE pu.user.id = :user AND pu.answered = false AND CURRENT_TIMESTAMP BETWEEN pu.poll.startDate AND pu.poll.endDate"),
    @NamedQuery(name="Poll.removeAnsweredPolls", query="DELETE FROM AnsweredPoll AS ap WHERE ap.poll.id = :id"),
    @NamedQuery(name="Poll.removeAnswers",query="DELETE FROM Answer AS a WHERE a.question.poll.id = :id"),
    @NamedQuery(name="Poll.removeAnswersForQuestion",query="DELETE FROM Answer AS a WHERE a.question = :question"),
    @NamedQuery(name="PollUser.toAnswer",query="SELECT pu FROM PollUser pu WHERE pu.user.id = :user AND pu.answered = false AND CURRENT_TIMESTAMP BETWEEN pu.poll.startDate AND pu.poll.endDate"),
    @NamedQuery(name="PollUser.byId",query="SELECT pu FROM PollUser pu WHERE pu.id = :id"),
    @NamedQuery(name="Answer.getAll",query="SELECT a FROM Answer a WHERE a.question.poll.id = :id"),
    @NamedQuery(name="TextAnswer.getAll",query="SELECT a FROM TextAnswer a WHERE a.question.poll.id = :id"),
    @NamedQuery(name="TextAnswer.getAllForAnsweredPoll",query="SELECT a FROM TextAnswer a WHERE a.question.poll.id = :id AND a.answeredPoll = :ap"),
    @NamedQuery(name="OptionAnswer.countAll",query="SELECT NEW hr.fer.zemris.jcms.beans.PollOptionBean(o, o.question, COUNT(a)) FROM OptionAnswer a RIGHT JOIN a.option o WHERE o.question.poll.id = :id GROUP BY o, o.question"),
    @NamedQuery(name="OptionAnswer.countAllForAnsweredPoll",query="SELECT NEW hr.fer.zemris.jcms.beans.PollOptionBean(o, o.question, COUNT(a)) FROM OptionAnswer a RIGHT JOIN a.option o WHERE o.question.poll.id = :id AND a.answeredPoll = :ap GROUP BY o, o.question"),
    @NamedQuery(name="Poll.getGroupsForPoll",query="SELECT DISTINCT pu.group FROM PollUser pu WHERE pu.poll.id = :id"),
    @NamedQuery(name="Poll.getAnsweredPollsForViewer",query="SELECT DISTINCT ap FROM AnsweredPoll ap, GroupOwner go WHERE ap.poll = :poll AND go.user = :user AND go.group=ap.group"),
    @NamedQuery(name="Poll.countAnsweredPollsForViewer",query="SELECT COUNT(DISTINCT ap) FROM AnsweredPoll ap, GroupOwner go WHERE ap.poll = :poll AND go.user = :user AND go.group=ap.group")
    
})
@Table(name="poll_polls")
public class Poll implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String title;
	private String description;
	private Date startDate;
	private Date endDate;
	private boolean viewablePublic = false; 
 	private Set<Question> questions = new HashSet<Question>(); // null?
 	private Set<User> owners = new HashSet<User>();
 	private PollTag pollTag;
 	
 	private Set<PollUser> pollUsers = new HashSet<PollUser>(); 
    
    @Version
    private int version;
    
 	public Poll() {
	}
 	
	public Poll(String title, String description, Date startDate, Date endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.title = title;
		this.description = description;
	}	

	public Poll(String title, String description, Date startDate, Date endDate, boolean viewablePublic) {
		super();
		this.title = title;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.viewablePublic = viewablePublic;
	}

	@Column(length=1000)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Id @GeneratedValue
	@Column
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column
    public boolean getViewablePublic() {
		return viewablePublic;
	}
	public void setViewablePublic(boolean viewablePublic) {
		this.viewablePublic = viewablePublic;
	}

	@OneToMany(mappedBy="poll",targetEntity=Question.class,cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},fetch=FetchType.LAZY)
	@OrderBy("ordinal")
	public Set<Question> getQuestions() {
		return questions;
	}
	public void setQuestions(Set<Question> questions) {
		this.questions = questions;
	}
	
	@Column
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Column(length=150)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name="poll_to_owners",
			joinColumns=@JoinColumn(name="poll_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="owner_id",referencedColumnName="id")
	)
	public Set<User> getOwners() {
		return owners;
	}
	public void setOwners(Set<User> owners) {
		this.owners = owners;
	}

	public boolean editable() {
		if(startDate==null) return false;
		if(startDate.after(Calendar.getInstance().getTime())) return true;
		return false;
	}
	public boolean active() {
		if(startDate==null || endDate==null) return false;
		if(startDate.before(Calendar.getInstance().getTime()) && endDate.after(Calendar.getInstance().getTime())) return true;
		return false;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public PollTag getPollTag() {
		return pollTag;
	}
	public void setPollTag(PollTag pollTag) {
		this.pollTag = pollTag;
	}

	@OneToMany(mappedBy="poll",targetEntity=PollUser.class,cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},fetch=FetchType.LAZY)
	public Set<PollUser> getPollUsers() {
		return pollUsers;
	}
	public void setPollUsers(Set<PollUser> pollUsers) {
		this.pollUsers = pollUsers;
	}

}
