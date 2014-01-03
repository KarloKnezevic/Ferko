package hr.fer.zemris.jcms.model.questions;

import java.util.HashSet;
import java.util.Set;

import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.forum.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Slika koji može biti vezana na više pitanja.
 * 
 * @author Alan Sambol
 */
@Entity
@Table(name = "questions_questionGraphics")
public class QuestionGraphics extends AbstractEntity {
	
	private Course course;
	private String filename;
	private String mimeType;
	private String description;
	private Set<QuestionVariant> questionVariants = new HashSet<QuestionVariant>();
	
	public QuestionGraphics() {
		
	}
	
	public QuestionGraphics(String filename, String mimeType,
			String description, Set<QuestionVariant> questionVariants) {
		super();
		this.filename = filename;
		this.mimeType = mimeType;
		this.description = description;
		this.questionVariants = questionVariants;
	}

	/**
	 * @return Naziv slike koju je korisnik uploadao
	 */
	@Column(nullable = false)
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * @return Mime-tip slike koji browser dobije prilikom uploada
	 */
	@Column(nullable = false, length = 100)
	public String getMimeType() {
		return mimeType;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * @return Opis slike koji korisnik zadaje prilikom uploada
	 */
	@Column(nullable = false, length = 500)
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return Varijante pitanja kojima je ova slika pridružena
	 */
	@ManyToMany(mappedBy = "questionGraphics", fetch = FetchType.LAZY)
	public Set<QuestionVariant> getQuestionVariants() {
		return questionVariants;
	}
	
	public void setQuestionVariants(Set<QuestionVariant> questionVariants) {
		this.questionVariants = questionVariants;
	}
	
	/**
	 * @return Primjerak kolegija kojemu pripada ova slika
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	public Course getCourse() {
		return course;
	}
	
	public void setCourse(Course course) {
		this.course = course;
	}
}
