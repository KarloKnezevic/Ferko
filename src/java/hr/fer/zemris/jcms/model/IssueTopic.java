package hr.fer.zemris.jcms.model;

import java.io.Serializable;

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

/**
 * Tema poruke/problema (issue topic)
 * Svaka poruka je vezana uz konkretnu temu tj. može imati samo jednu temu. 
 * 
 * Primjeri tema su:
 * <ul>
 * <li>Predavanja</li>
 * <li>1. laboratorijska vježba</li>
 * <li>2. domaća zadaća</li>
 * <li>Prvi međuispit</li>
 * <li>Nadoknada prvog međuispita</li>
 * <li>Završni ispit</li>
 * <li>Ponovljeni završni ispit</li>
 * <li>Druga automatska provjera znanja</li>
 * <li>itd....</li>
 * </ul>
 * 
 * NAPOMENA: U kontekstu Ferkovog issue-tracking sustava, termini: poruka, pitanje, problem, issue i message su EKVIVALENTNI
 */
@Entity
@Table(name="issue_topics")
@NamedQueries({
    @NamedQuery(name="IssueTopic.listForCourseInstance",query="select m from IssueTopic as m where m.courseInstance.id=:courseInstanceID"),
    @NamedQuery(name="IssueTopic.listActiveForCourseInstance",query="select m from IssueTopic as m where m.courseInstance.id=:courseInstanceID AND active = 1"),
    @NamedQuery(name="IssueTopic.findTopic", query="select m from IssueTopic as m where m.courseInstance.id = :courseInstanceID AND m.name = :topicName"),
    @NamedQuery(name="IssueTopic.getTopicByID", query="select m from IssueTopic as m where m.id = :topicID")
})
public class IssueTopic implements Serializable, Comparable<IssueTopic>{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	private boolean active;
	private String shortName; 
	private CourseInstance courseInstance;
	
	public IssueTopic() {
	}

	/**
	 * Identifikator.
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
	 * Je li tema aktivna (true). Samo aktivne teme se prikazuju
	 * korisnicima i oni ih mogu odabrati. Neaktivne se ne prikazuju.
	 * @return true ako je tema aktivna, false inače
	 */
	public boolean getActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Naziv teme.
	 * @return naziv
	 */
	@Column(length=200, unique=false, nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(length=10, unique=false, nullable=true)
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(nullable=false)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IssueTopic))
			return false;
		IssueTopic other = (IssueTopic) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	@Override
	public int compareTo(IssueTopic arg0) { 
		return this.getName().compareTo(arg0.getName());
	}

	
}
