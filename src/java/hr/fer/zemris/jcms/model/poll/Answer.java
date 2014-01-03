package hr.fer.zemris.jcms.model.poll;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name="poll_answers")
@Inheritance(strategy=InheritanceType.JOINED)
public class Answer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
    private Question question;
    private AnsweredPoll answeredPoll;
    
    @Version
    private int version;
    
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(nullable=false)
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
	}
	
	@Id @GeneratedValue
	@Column
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setAnsweredPoll(AnsweredPoll answeredPoll) {
		this.answeredPoll = answeredPoll;
	}
    @ManyToOne(fetch=FetchType.EAGER, optional=true)
    @JoinColumn(nullable=true)
	public AnsweredPoll getAnsweredPoll() {
		return answeredPoll;
	}
	
    @Transient
    public String getAnswerText() {
    	return null;
    }

}
