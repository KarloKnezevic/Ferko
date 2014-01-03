package hr.fer.zemris.jcms.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Where;


/**
 * Razred predstavlja repozitorij u jednom predmetu. 
 * 
 * 
 */


@NamedQueries({
    @NamedQuery(name="RepositoryCourse.findForCourse",query="select rcou from RepositoryCourse as rcou where rcou.course=:course")
    //QUERIES TODO
})
@Entity
@Table(name="course_repositories")
public class RepositoryCourse implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* Predlozeni model:
  		*Repository
			+-id
			+-course
			+-rootCategories
	 */
	
	private Long id;
	private Course course;
	private List<RepositoryCategory> rootCategories = new ArrayList<RepositoryCategory>();
	
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}	
	
	@OneToOne(fetch=FetchType.EAGER,mappedBy="repository")
	@Fetch(FetchMode.SELECT)
	public Course getCourse() {
		return course;
	}
	
	public void setCourse(Course course) {
		this.course = course;
	}
	
	@OneToMany(fetch=FetchType.EAGER,mappedBy="repositoryCourse")
	@OrderBy("position")
	@Fetch(FetchMode.SELECT)
	@Where(clause="parentCategory_id is null") 
	public List<RepositoryCategory> getRootCategories() {
		return rootCategories;
	}

	public void setRootCategories(List<RepositoryCategory> rootCategories) {
		this.rootCategories = rootCategories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCourse() == null) ? 0 : getCourse().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RepositoryCourse))
			return false;
		RepositoryCourse other = (RepositoryCourse) obj;
		if (getCourse() == null) {
			if (other.getCourse() != null)
				return false;
		} else if (!getCourse().equals(other.getCourse()))
			return false;
		return true;
	}
	
}
