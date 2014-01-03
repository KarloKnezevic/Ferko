package hr.fer.zemris.jcms.model.poll;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
@DiscriminatorValue("S")
public class SingleChoiceQuestion extends Question {

	private static final long serialVersionUID = 1L;
	
  	private Set<Option> options = new LinkedHashSet<Option>();
  	
    public SingleChoiceQuestion() {
	}

	public SingleChoiceQuestion(String questionText, Integer ordinal) {
		this.questionText = questionText;
		this.ordinal = ordinal;
	}
  	
    @OneToMany(mappedBy="question",targetEntity=Option.class,cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},fetch=FetchType.LAZY)
    @OrderBy("ordinal")
	public Set<Option> getOptions() {
		return options;
	}
	public void setOptions(Set<Option> options) {
		this.options = options;
	}
	
	public String type() {
		return "singlechoice";
	}

}
