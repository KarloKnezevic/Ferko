package hr.fer.zemris.jcms.model.poll;

import hr.fer.zemris.jcms.model.Group;

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
@Table(name="poll_answered_polls")
public class AnsweredPoll implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Group group;
	private Poll poll;
	
    @Version
    private int version;
    
	@Id @GeneratedValue
	@Column
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Poll getPoll() {
		return poll;
	}
	public void setPoll(Poll poll) {
		this.poll = poll;
	}
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
