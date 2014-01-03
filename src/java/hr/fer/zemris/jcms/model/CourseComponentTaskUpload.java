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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_task_upload")
public class CourseComponentTaskUpload implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CourseComponentTaskAssignment courseComponentTaskAssignment;
	private String mimeType;
	private String charset;
	private String fileName;
	private String tag;
	private Date uploadedOn;  
	
	public CourseComponentTaskUpload() {
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
	public CourseComponentTaskAssignment getCourseComponentTaskAssignment() {
		return courseComponentTaskAssignment;
	}

	public void setCourseComponentTaskAssignment(
			CourseComponentTaskAssignment courseComponentTaskAssignment) {
		this.courseComponentTaskAssignment = courseComponentTaskAssignment;
	}
	
	@Column(length=40)
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Column(length=20)
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Column(length=100)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(length=10)
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getUploadedOn() {
		return uploadedOn;
	}

	public void setUploadedOn(Date uploadedOn) {
		this.uploadedOn = uploadedOn;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCourseComponentTaskAssignment() == null) ? 0
						: getCourseComponentTaskAssignment().hashCode());
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
		if (!(obj instanceof CourseComponentTaskUpload))
			return false;
		final CourseComponentTaskUpload other = (CourseComponentTaskUpload) obj;
		if (getCourseComponentTaskAssignment() == null) {
			if (other.getCourseComponentTaskAssignment() != null)
				return false;
		} else if (!getCourseComponentTaskAssignment()
				.equals(other.getCourseComponentTaskAssignment()))
			return false;
		if (getFileName() == null) {
			if (other.getFileName() != null)
				return false;
		} else if (!getFileName().equals(other.getFileName()))
			return false;
		return true;
	}
}
