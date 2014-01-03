package hr.fer.zemris.jcms.model.poll;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="poll_text_answers")
public class TextAnswer extends Answer implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String answer; 
    
    public TextAnswer() {
	}
    
	public TextAnswer(String answer) {
		super();
		this.answer = answer;
	}
	
	@Column(length=2000)
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	@Transient
	public String getAnswerText() {
		return getAnswer();
	}

}
