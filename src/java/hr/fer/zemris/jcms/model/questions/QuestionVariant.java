package hr.fer.zemris.jcms.model.questions;

import hr.fer.zemris.jcms.model.forum.AbstractEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Varijanta pitanja smještena unutar grupe pitanja.
 * 
 * @author Alan Sambol
 */
@Entity
@Table(name = "questions_questionVariants")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("*")
@NamedQueries( { @NamedQuery(name = "QuestionVariant.byQuestionGroup", query = "SELECT q from QuestionVariant as q WHERE q.questionGroup = :questionGroup") })
public abstract class QuestionVariant extends AbstractEntity {

	private QuestionGroup questionGroup;
	private String questionText;
	private Set<QuestionTag> questionTags = new HashSet<QuestionTag>();
	private Set<QuestionGraphics> questionGraphics = new HashSet<QuestionGraphics>();

	/**
	 * @return Primjerak grupe pitanja kojoj pripada ova varijanta pitanja.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	public QuestionGroup getQuestionGroup() {
		return questionGroup;
	}
	
	public void setQuestionGroup(QuestionGroup questionGroup) {
		this.questionGroup = questionGroup;
	}
	
	/**
	 * @return Tekst pitanja.
	 */
	@Column(nullable = false, length = 4000)
	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}	

	/**
	 * @return Tagovi koji pripadaju ovom pitanju.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "questions_t_qv", joinColumns = @JoinColumn(name = "QuestionVariant_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "QuestionTag_id", referencedColumnName = "id"))
	public Set<QuestionTag> getQuestionTags() {
		return questionTags;
	}

	public void setQuestionTags(Set<QuestionTag> questionTags) {
		this.questionTags = questionTags;
	}

	/**
	 * @return Slike koje pripadaju ovom pitanju.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "questions_qg_qv", joinColumns = @JoinColumn(name = "QuestionVariant_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "QuestionGraphics_id", referencedColumnName = "id"))
	public Set<QuestionGraphics> getQuestionGraphics() {
		return questionGraphics;
	}

	public void setQuestionGraphics(Set<QuestionGraphics> questionGraphics) {
		this.questionGraphics = questionGraphics;
	}	

}
