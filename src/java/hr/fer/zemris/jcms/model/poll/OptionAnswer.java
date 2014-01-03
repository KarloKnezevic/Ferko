package hr.fer.zemris.jcms.model.poll;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="poll_option_answers")
public class OptionAnswer extends Answer implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Option option; 
    
    public OptionAnswer() {
	}
    
	public OptionAnswer(Option option) {
		super();
		this.option = option;
	}

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(nullable=false)
	public Option getOption() {
		return option;
	}
	public void setOption(Option option) {
		this.option = option;
	}
	
	@Transient
	public String getAnswerText() {
		return getOption().getText();
	}

}
