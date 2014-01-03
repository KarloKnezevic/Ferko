package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="item_description_file",uniqueConstraints={
	@UniqueConstraint(columnNames={"courseComponentItem_id","fileName"})	
})
public class ItemDescriptionFile implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CourseComponentItem courseComponentItem;
	private String fileName;
	private String charset;
	private String mimeType;
	
	public ItemDescriptionFile() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponentItem getCourseComponentItem() {
		return courseComponentItem;
	}
	public void setCourseComponentItem(CourseComponentItem courseComponentItem) {
		this.courseComponentItem = courseComponentItem;
	}
	
	@Column(nullable=false,length=100)
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Column(length=20)
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	@Column(length=40)
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCourseComponentItem() == null) ? 0 : getCourseComponentItem()
						.hashCode());
		result = prime * result
				+ ((getFileName() == null) ? 0 : getFileName().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ItemDescriptionFile))
			return false;
		final ItemDescriptionFile other = (ItemDescriptionFile) obj;
		if (getCourseComponentItem() == null) {
			if (other.getCourseComponentItem() != null)
				return false;
		} else if (!getCourseComponentItem().equals(other.getCourseComponentItem()))
			return false;
		if (getFileName() == null) {
			if (other.getFileName() != null)
				return false;
		} else if (!getFileName().equals(other.getFileName()))
			return false;
		return true;
	}
	
	
}
