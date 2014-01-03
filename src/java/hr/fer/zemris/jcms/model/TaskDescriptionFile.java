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
@Table(name="task_description_file",uniqueConstraints={
	@UniqueConstraint(columnNames={"courseComponentTask_id","fileName"})	
})
public class TaskDescriptionFile implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private CourseComponentTask courseComponentTask;
	private String fileName;
	private String charset;
	private String mimeType;
	
	public TaskDescriptionFile() {
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
	public CourseComponentTask getCourseComponentTask() {
		return courseComponentTask;
	}
	public void setCourseComponentTask(CourseComponentTask courseComponentTask) {
		this.courseComponentTask = courseComponentTask;
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
				+ ((getCourseComponentTask() == null) ? 0 : getCourseComponentTask()
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
		if (!(obj instanceof TaskDescriptionFile))
			return false;
		final TaskDescriptionFile other = (TaskDescriptionFile) obj;
		if (getCourseComponentTask() == null) {
			if (other.getCourseComponentTask() != null)
				return false;
		} else if (!getCourseComponentTask().equals(other.getCourseComponentTask()))
			return false;
		if (getFileName() == null) {
			if (other.getFileName() != null)
				return false;
		} else if (!getFileName().equals(other.getFileName()))
			return false;
		return true;
	}
}
