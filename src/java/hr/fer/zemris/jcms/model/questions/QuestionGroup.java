package hr.fer.zemris.jcms.model.questions;

import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.forum.AbstractEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Sadrži podatke o grupi pitanja. Ona pripada jednom predmetu i sadrži varijante pitanja.
 * 
 * @author Alan Sambol
 */
@Entity
@Table(name = "questions_questionGroups", uniqueConstraints = {
	@UniqueConstraint(columnNames = { "course_isvuCode", "name" })
})
@NamedQueries({ 
	@NamedQuery(name = "QuestionGroup.byName", query = "SELECT g from QuestionGroup as g WHERE g.name = :name"),
	@NamedQuery(name = "QuestionGroup.byCourse", query = "SELECT g from QuestionGroup as g WHERE g.course = :course") 
})
public class QuestionGroup extends AbstractEntity {

	private Course course;
	private String name;
	private Set<QuestionVariant> questionVariants;
	private Set<QuestionTag> groupTags = new HashSet<QuestionTag>();
	private double weight = 1.0;
	
	/**
	 * Predviđena težina varijanti zadataka koje se nalaze u ovoj grupi.
	 * 
	 * @return predviđena težina
	 */
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @return Primjerak kolegija kojemu pripada ova grupa pitanja.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	public Course getCourse() {
		return course;
	}
	
	public void setCourse(Course course) {
		this.course = course;
	}
	
	/**
	 * @return Ime grupe pitanja.
	 */
	@Column(nullable = false, length = 60)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Varijante pitanja koje pripadaju ovoj grupi.
	 */
	@OneToMany(mappedBy = "questionGroup", cascade = { CascadeType.REMOVE })
	public Set<QuestionVariant> getQuestionVariants() {
		return questionVariants;
	}

	public void setQuestionVariants(Set<QuestionVariant> questionVariants) {
		this.questionVariants = questionVariants;
	}

	/**
	 * @return Tagovi koji pripadaju ovoj grupi.
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "questions_t_qg", joinColumns = @JoinColumn(name = "QuestionGroup_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "QuestionTag_id", referencedColumnName = "id"))
	public Set<QuestionTag> getGroupTags() {
		return groupTags;
	}

	public void setGroupTags(Set<QuestionTag> groupTags) {
		this.groupTags = groupTags;
	}

}
