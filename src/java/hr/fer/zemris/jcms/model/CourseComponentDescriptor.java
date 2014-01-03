package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Razred koji opisuje jednu komponentu
 * 
 * @author TOMISLAV
 *
 */

@Entity
@Table(name="course_component_descriptors")
@NamedQueries({
    @NamedQuery(name="CourseComponentDescriptor.list",query="select ccd from CourseComponentDescriptor as ccd"),
    @NamedQuery(name="CourseComponentDescriptor.getByShortName",query="select ccd from CourseComponentDescriptor as ccd where ccd.shortName=:shortName")
})
public class CourseComponentDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String shortName;
	private String name;
	private String positionalName;
	private String groupRoot;
	
	public CourseComponentDescriptor() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Kratko ime, ujedno i jezgra za imenovanje povezanih ispita i sl.
	 * @return
	 */
	@Column(length=10,nullable=false,unique=true)
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	/**
	 * Normalno ime, npr. "Laboratorijske vježbe"
	 * @return
	 */
	@Column(length=50, nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Ime u obliku prikladnom za numeraciju: "laboratorijska vježba" 
	 * - koristit će se za konstrukcije tipa "5. laboratorijska vježba"
	 * @return
	 */
	@Column(length=50, nullable=false)
	public String getPositionalName() {
		return positionalName;
	}
	public void setPositionalName(String positionalName) {
		this.positionalName = positionalName;
	}
	
	/**
	 * Oznaka vršne grupe koja će se koristiti za ovu komponentu; primjerice, za labose će to biti 2
	 * @return
	 */
	@Column(length=10, nullable=false)
	public String getGroupRoot() {
		return groupRoot;
	}
	public void setGroupRoot(String groupRoot) {
		this.groupRoot = groupRoot;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getShortName() == null) ? 0 : getShortName().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponentDescriptor))
			return false;
		final CourseComponentDescriptor other = (CourseComponentDescriptor) obj;
		if (getShortName() == null) {
			if (other.getShortName() != null)
				return false;
		} else if (!getShortName().equals(other.getShortName()))
			return false;
		return true;
	}	
}
