package hr.fer.zemris.jcms.model.poll;

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
@Table(name="poll_options")
public class Option implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String text;
	private Integer ordinal = 0;
	private Question question;
	
	public Option() {
	}
	
	public Option(String text, Integer ordinal) {
		super();
		this.text = text;
		this.ordinal = ordinal;
	} 
	
    @Version
    private int version;
	
	@Column
	public Integer getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(Integer ordinal) {
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
	
	@Column
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
    @ManyToOne(optional=false)
    @JoinColumn(nullable=false)
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
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
		Option other = (Option) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
