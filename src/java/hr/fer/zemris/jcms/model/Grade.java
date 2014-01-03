package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Ocjene na kolegiju. Nalazi se u N-na-1 relaciji s primjerkom kolegija.
 * 
 * @author marcupic
 */
@NamedQueries({
	@NamedQuery(name="Grade.forCourseInstance",query="select g from Grade as g where g.courseInstance=:ci"),
	@NamedQuery(name="Grade.forCourseInstanceAndUser",query="select g from Grade as g where g.courseInstance=:ci and g.user=:user")
})
@Entity
@Table(name="grades",uniqueConstraints={
		@UniqueConstraint(columnNames={"courseInstance_id","user_id"})
})
public class Grade implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private CourseInstance courseInstance;
	private User user;
	private Date givenAt;
	private User givenBy;
	private byte grade;
	
	/**
	 * Primarni ključ.
	 * 
	 * @return identifikator
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Primjerak kolegija na kojem je dana ova ocjena.
	 * 
	 * @return primjerak kolegija
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	
	/**
	 * Student čija je ovo ocjena.
	 * 
	 * @return student
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Kada je ocjenjivanje napravljeno?
	 * 
	 * @return vrijeme ocjenjivanja
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable=false)
	public Date getGivenAt() {
		return givenAt;
	}
	public void setGivenAt(Date givenAt) {
		this.givenAt = givenAt;
	}
	
	/**
	 * Tko je napravio ocjenjivanje?
	 * 
	 * @return korisnik koji je napravio ocjenjivanje
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	public User getGivenBy() {
		return givenBy;
	}
	public void setGivenBy(User givenBy) {
		this.givenBy = givenBy;
	}
	
	/**
	 * Ocjena.
	 * 
	 * @return ocjena
	 */
	public byte getGrade() {
		return grade;
	}
	public void setGrade(byte grade) {
		this.grade = grade;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((courseInstance == null) ? 0 : courseInstance.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grade other = (Grade) obj;
		if (courseInstance == null) {
			if (other.courseInstance != null)
				return false;
		} else if (!courseInstance.equals(other.courseInstance))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}
