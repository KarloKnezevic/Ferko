package hr.fer.zemris.jcms.beans;

public class CourseInstanceBean {

	private String id;
	private String yearSemesterID;
	private String courseID;
	private String primaryGroupID;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getYearSemesterID() {
		return yearSemesterID;
	}
	public void setYearSemesterID(String yearSemesterID) {
		this.yearSemesterID = yearSemesterID;
	}
	public String getCourseID() {
		return courseID;
	}
	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}
	public String getPrimaryGroupID() {
		return primaryGroupID;
	}
	public void setPrimaryGroupID(String primaryGroupID) {
		this.primaryGroupID = primaryGroupID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseInstanceBean))
			return false;
		final CourseInstanceBean other = (CourseInstanceBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
