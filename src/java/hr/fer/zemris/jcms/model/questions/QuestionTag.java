package hr.fer.zemris.jcms.model.questions;

import java.util.HashSet;
import java.util.Set;

import hr.fer.zemris.jcms.model.forum.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * QuestionTag koji može biti vezan uz grupu pitanja ili uz pojedinačno pitanje.
 * 
 * @author Alan Sambol
 */
@Entity
@Table(name = "questions_tags")
@NamedQueries( { @NamedQuery(name = "QuestionTag.byTagText", query = "SELECT t from QuestionTag as t WHERE t.tagText = :tagText") })
public class QuestionTag extends AbstractEntity {
	
	private String tagText;
	private Set<QuestionGroup> tagGroups = new HashSet<QuestionGroup>();
	private Set<QuestionVariant> tagQuestions = new HashSet<QuestionVariant>();
	
	public QuestionTag() {
		
	}
	
	public QuestionTag(String tagText) {
		this.tagText = tagText;
	}
	
	/**
	 * @return Tekst taga.
	 */
	@Column(nullable = false, length = 100, unique = true)
	public String getTagText() {
		return tagText;
	}

	public void setTagText(String tagText) {
		this.tagText = tagText;
	}
	
	/**
	 * @return Grupe koje sadrže ovaj tag.
	 */	
	@ManyToMany(mappedBy = "groupTags", fetch = FetchType.LAZY)
	public Set<QuestionGroup> getTagGroups() {
		return tagGroups;
	}

	public void setTagGroups(Set<QuestionGroup> tagGroups) {
		this.tagGroups = tagGroups;
	}
	
	/**
	 * @return Pitanja koja sadrže ovaj tag.
	 */
	@ManyToMany(mappedBy = "questionTags", fetch = FetchType.LAZY)
	public Set<QuestionVariant> getTagQuestions() {
		return tagQuestions;
	}

	public void setTagQuestions(Set<QuestionVariant> tagQuestions) {
		this.tagQuestions = tagQuestions;
	}
	
	/**
	 * Služi za usporedbu 2 taga samo na temelju njihovog teksta.
	 * @param other Drugi tag s kojim uspoređujemo.
	 * @return Vraća true ako oba taga imaju jednaki tekst. 
	 */	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof QuestionTag)) return false;
		QuestionTag drugi = (QuestionTag)other;
		if(this.getTagText().equals(drugi.getTagText())) return true;
		return false;
	}
}
