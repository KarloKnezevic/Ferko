package hr.fer.zemris.jcms.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


/**
 * Razred predstavlja jednu kategoriju repozitorija u jednom predmetu. 
 * 
 * 
 */

@NamedQueries({
    @NamedQuery(name="RepositoryCategory.listForRepositoryCourse",query="select rcat from RepositoryCategory as rcat where rcat.repositoryCourse=:repositoryCourse")
    //QUERIES TODO
})
@Entity
@Table(name="repository_categories",uniqueConstraints={
		// Ne mogu postojati dva djeteta istog roditelja s istim imenom (categoryName) na istom kolegiju
		@UniqueConstraint(columnNames={"repositoryCourse_id","parentCategory_id","categoryName"})
})
public class RepositoryCategory implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/* Predlozeni model:
  		RepositoryCategory
  			+- id
  			+- name
  			+- repozitorij
  			+- parent kategorija
	 		+- subcategories 
	 		+- position - služi za ispis, up,down
	 		+- files -sve datoteke u trenutnoj kategoriji
	 */
	
	private Long id;
	private String categoryName;
	private RepositoryCourse repositoryCourse;
	private RepositoryCategory parentCategory;
	private List<RepositoryCategory> subCategories = new ArrayList<RepositoryCategory>();
	private int position; 
	private List<RepositoryFile> files = new ArrayList<RepositoryFile>();
	
	
	
	@OneToMany(fetch=FetchType.EAGER,mappedBy="category")
	@Fetch(FetchMode.SELECT)
	public List<RepositoryFile> getFiles() {
		return files;
	}


	public void setFiles(List<RepositoryFile> files) {
		this.files = files;
	}

	@OneToMany(fetch=FetchType.EAGER,mappedBy="parentCategory")
	@OrderBy("position")
	@Fetch(FetchMode.SELECT)
	public List<RepositoryCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<RepositoryCategory> subCategories) {
		this.subCategories = subCategories;
	}

	@Column(nullable=false)
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}	
	
	@Column(length = 100, nullable = false)
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	//@JoinColumn(nullable=false)
	public RepositoryCourse getRepositoryCourse() {
		return repositoryCourse;
	}

	public void setRepositoryCourse(RepositoryCourse repositoryCourse) {
		this.repositoryCourse = repositoryCourse;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public RepositoryCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(RepositoryCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getCategoryName() == null) ? 0 : getCategoryName().hashCode());
		result = prime * result
				+ ((getParentCategory() == null) ? 0 : getParentCategory().hashCode());
		result = prime
				* result
				+ ((getRepositoryCourse() == null) ? 0 : getRepositoryCourse().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RepositoryCategory))
			return false;
		RepositoryCategory other = (RepositoryCategory) obj;
		if (getCategoryName() == null) {
			if (other.getCategoryName() != null)
				return false;
		} else if (!getCategoryName().equals(other.getCategoryName()))
			return false;
		
		if (getParentCategory() == null) {
			if (other.getParentCategory() != null)
				return false;
		} else if (!getParentCategory().equals(other.getParentCategory()))
			return false;
		if (getRepositoryCourse() == null) {
			if (other.getRepositoryCourse() != null)
				return false;
		} else if (!getRepositoryCourse().equals(other.getRepositoryCourse()))
			return false;
		return true;
	}	
	
	

}
