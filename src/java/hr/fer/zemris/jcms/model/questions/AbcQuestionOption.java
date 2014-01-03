package hr.fer.zemris.jcms.model.questions;

import hr.fer.zemris.jcms.model.forum.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Odgovor na abc pitalicu.
 * @author Alan Sambol
 */
@Entity
@Table(name = "questions_questionOptions")
@NamedQueries( { @NamedQuery(name = "AbcQuestionOption.byQuestion", query = "SELECT o from AbcQuestionOption as o WHERE o.question = :question"),
				@NamedQuery(name = "AbcQuestionOption.byOptionText", query = "SELECT o from AbcQuestionOption as o WHERE o.optionText = :optionText") })
public class AbcQuestionOption extends AbstractEntity {
	
	private AbcQuestion question;
	private String optionText;
	private boolean correctOption;
	private boolean preferredOption;
	private boolean defaultOption;
	
	public AbcQuestionOption() {
		
	}
	
	public AbcQuestionOption(String optionText, boolean correctOption, boolean preferredOption, boolean defaultOption) {
		this.optionText = optionText;
		this.correctOption = correctOption;
		this.preferredOption = preferredOption;
		this.defaultOption = defaultOption;
	}
	
	
	/**
	 * @return Pitanje kojem ovaj odgovor pripada
	 */
	@ManyToOne
	@JoinColumn(nullable = false)
	public AbcQuestion getQuestion() {
		return question;
	}

	public void setQuestion(AbcQuestion question) {
		this.question = question;
	}
	
	
	/**
	 * @return Tekst odgovora na pitanje
	 */
	@Column(nullable = false, length = 4000)
	public String getOptionText() {
		return optionText;
	}
	
	public void setOptionText(String optionText) {
		this.optionText = optionText;
	}
	
	/**
	 * @return Je li ovaj odgovor točan
	 */
	@Column(nullable = false)
	public boolean getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(boolean correctOption) {
		this.correctOption = correctOption;
	}
	
	/**
	 * @return Je li ovaj odgovor preferiran
	 */
	@Column(nullable = false)
	public boolean getPreferredOption() {
		return preferredOption;
	}

	public void setPreferredOption(boolean preferredOption) {
		this.preferredOption = preferredOption;
	}
	
	/**
	 * @return Je li ovaj odgovor defaultan
	 */
	@Column(nullable = false)
	public boolean getDefaultOption() {
		return defaultOption;
	}

	public void setDefaultOption(boolean defaultOption) {
		this.defaultOption = defaultOption;
	}
	
	/**
	 * Služi za usporedbu 2 odgovora na temelju njihovog teksta i zastavica.
	 * @param other Drugi odgovor s kojim uspoređujemo.
	 * @return Vraća true ako oba odgovora imaju jednaki tekst i zastavice. 
	 */	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof AbcQuestionOption)) return false;
		AbcQuestionOption drugi = (AbcQuestionOption)other;
		if(this.getOptionText().equals(drugi.getOptionText()) && this.getCorrectOption() == drugi.getCorrectOption() && this.getDefaultOption() == drugi.getDefaultOption() && this.getPreferredOption() == drugi.getPreferredOption()) 
			return true;
		return false;
	}
}
