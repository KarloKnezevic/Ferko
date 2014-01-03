package hr.fer.zemris.jcms.model.poll;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="poll_questions")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type",discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("*")
public class Question implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	protected Integer ordinal = 0;
	protected String questionText;
	protected String validation;
    private Poll poll;
    
    @Version
    private int version;
    
    public Question() {
	}

	public Question(String questionText, Integer ordinal) {
		super();
		this.questionText = questionText;
		this.ordinal = ordinal;
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

	@Column
	public Integer getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
	
	@Column(length=256)
	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	
	@Column(length=64)
	public String getValidation() {
		return validation;
	}
	public void setValidation(String validation) {
		this.validation = validation;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public int hashCode() {
		if(id==null) return super.hashCode();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(id==null) return super.equals(obj);
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
