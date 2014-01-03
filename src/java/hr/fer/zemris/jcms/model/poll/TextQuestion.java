package hr.fer.zemris.jcms.model.poll;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("T")
public class TextQuestion extends Question {

	private static final long serialVersionUID = 1L;
   	//private Set<TextAnswer> textAnswers = new HashSet<TextAnswer>();
   	
    public TextQuestion() {
	}

	public TextQuestion(String questionText, Integer ordinal) {
		this.questionText = questionText;
		this.ordinal = ordinal;
	}
   	/*
	@OneToMany(mappedBy="question",targetEntity=TextAnswer.class,cascade={CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},fetch=FetchType.LAZY)
	public Set<TextAnswer> getTextAnswers() {
		return textAnswers;
	}
	public void setTextAnswers(Set<TextAnswer> textAnswers) {
		this.textAnswers = textAnswers;
	}
	*/
	
	public String type() {
		return "text";
	}
}
