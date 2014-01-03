package hr.fer.zemris.jcms.beans;

public class YearSemesterBean {

	private String id;
	private String academicYear;
	private String semester;
	private String startsAt;
	private String endsAt;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAcademicYear() {
		return academicYear;
	}
	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}
	public String getSemester() {
		return semester;
	}
	public void setSemester(String semester) {
		this.semester = semester;
	}

	public String getStartsAt() {
		return startsAt;
	}
	public void setStartsAt(String startsAt) {
		this.startsAt = startsAt;
	}
	public String getEndsAt() {
		return endsAt;
	}
	public void setEndsAt(String endsAt) {
		this.endsAt = endsAt;
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
		if (!(obj instanceof YearSemesterBean))
			return false;
		final YearSemesterBean other = (YearSemesterBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
