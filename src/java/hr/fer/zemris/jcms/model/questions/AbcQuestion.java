package hr.fer.zemris.jcms.model.questions;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Pitanje u obliku abc pitalice smješteno unutar grupe pitanja.
 * @author Alan Sambol
 */
@Entity
@DiscriminatorValue("A")
public class AbcQuestion extends QuestionVariant {

	private Set<AbcQuestionOption> questionOptions = new HashSet<AbcQuestionOption>();
	
	/**
	 * @return Odgovori na abc pitalicu.
	 */
	@OneToMany(mappedBy = "question", cascade = { CascadeType.REMOVE })
	public Set<AbcQuestionOption> getQuestionOptions() {
		return questionOptions;
	}

	public void setQuestionOptions(Set<AbcQuestionOption> questionOptions) {
		this.questionOptions = questionOptions;
	}
}
