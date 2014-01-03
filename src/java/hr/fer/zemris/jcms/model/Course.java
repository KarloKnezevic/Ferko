package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.model.forum.Category;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Razred predstavlja jedan kolegij. Kolegij je zadan ISVU sifrom i imenom.
 * 
 * @author marcupic
 *
 */
@NamedQueries({
    @NamedQuery(name="Course.list",query="select c from Course as c"),
    @NamedQuery(name="Course.listAllWithoutCategory",query="select c from Course c left join c.category cat where cat is null")
})
@Entity
@Table(name="courses")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Course implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	private String isvuCode;
	private RepositoryCourse repository;
	private Category category;

	public Course() {
	}
	
	@Column(nullable=false,length=100,unique=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Primarni ključ objekta. Odgovara ISVU šifri kolegija.
	 * @return
	 */
	@Id
	@Column(length=10)
	public String getIsvuCode() {
		return isvuCode;
	}
	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}
	
	/**
	 * @return Kategorija ovog kolegija.
	 */
	@OneToOne(mappedBy = "course")
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIsvuCode() == null) ? 0 : getIsvuCode().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Course))
			return false;
		final Course other = (Course) obj;
		if (getIsvuCode() == null) {
			if (other.getIsvuCode() != null)
				return false;
		} else if (!getIsvuCode().equals(other.getIsvuCode()))
			return false;
		return true;
	}

	
	public void setRepository(RepositoryCourse repository) {
		this.repository = repository;
	}

	@OneToOne(fetch=FetchType.EAGER,cascade={CascadeType.ALL})
	@Fetch(FetchMode.SELECT)
	public RepositoryCourse getRepository() {
		return repository;
	}
}
