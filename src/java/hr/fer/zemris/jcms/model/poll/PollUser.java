package hr.fer.zemris.jcms.model.poll;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="poll_poll_users")
public class PollUser implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private User user;
	private boolean answered = false;
	private Group group;
    private Poll poll;
	
    @Version
    private int version;
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Column
	public boolean getAnswered() {
		return answered;
	}
	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
	
    @ManyToOne(optional=true)
    @JoinColumn(nullable=true)
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	
    @ManyToOne(optional=false)
    @JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Id @GeneratedValue
	@Column
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
    @ManyToOne(optional=false)
    @JoinColumn(nullable=false)
	public Poll getPoll() {
		return poll;
	}
	public void setPoll(Poll poll) {
		this.poll = poll;
	}
	
}
