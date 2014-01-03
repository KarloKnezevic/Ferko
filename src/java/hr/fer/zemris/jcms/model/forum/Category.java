package hr.fer.zemris.jcms.model.forum;

import hr.fer.zemris.jcms.model.Course;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;

/**
 * Kategorija pripada jednom predmetu i sadrži podforume.
 * 
 * @author Hrvoje Ban
 */
@Entity
@Table(name = "forum_categories")
@NamedQueries({
	@NamedQuery(name = "Category.nonCourse", query= "SELECT c FROM Category c WHERE c.course IS NULL"),
	@NamedQuery(name = "Category.nonCourseNonHidden", 
		query= "SELECT c FROM Category c WHERE c.course IS NULL AND c.hidden=false"),
	@NamedQuery(name = "Category.all", query = "SELECT c FROM Category c")
})
@Validation
public class Category extends AbstractEntity {

	private Course course;
	private String name;
	private boolean closed;
	private boolean hidden;
	private Set<Subforum> subforums;

	/**
	 * @return Primjerak kolegija kojemu pripada ova kategorija.
	 */
	@OneToOne(fetch = FetchType.EAGER)
	public Course getCourse() {
		return course;
	}
	
	public void setCourse(Course course) {
		this.course = course;
	}
	
	/**
	 * @return Ime kategorije. Koristi se samo ako kategorija ne pripada nijednom
	 * primjerku kolegija.
	 */
	@Column(length = 64)
	@FieldExpressionValidator(expression = "course != null || (course == null && name != null && name.length() != 0)", message = "Ime ne smije biti prazno")
	@StringLengthFieldValidator(minLength = "4", maxLength = "64", message = "Ime nije dopuštene dužine")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Ime koje treba prikazati za ovu kategoriju. Ako kategorija pripada
	 * kolegiju vraća ime kolegija, inaće vraća ime kategorije.
	 */
	@Transient
	public String getDisplayName() {
		if (course != null)
			return course.getName();
		else
			return this.getName();
	}
	
	/**
	 * @return Jeli moguće stvarati nove teme i poruke te uređivati postojeće
	 * unutar ove kategorije.
	 */
	@Column(nullable = false)
	public boolean isClosed() {
		return closed;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	/**
	 * @return Jeli kategorija vidljiva korisnicima koji nisu administratori ili
	 * moderatori.
	 */
	@Column(nullable = false)
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return Popis podforuma unutar ove kategorije.
	 */
	@OneToMany(mappedBy = "category", cascade = { CascadeType.REMOVE })
	public Set<Subforum> getSubforums() {
		return subforums;
	}

	public void setSubforums(Set<Subforum> subforums) {
		this.subforums = subforums;
	}

}
