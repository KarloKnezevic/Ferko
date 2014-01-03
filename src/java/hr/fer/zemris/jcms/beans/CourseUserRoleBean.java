package hr.fer.zemris.jcms.beans;

public class CourseUserRoleBean {
	private String courseInstanceID;
	private String userJMBAG;
	private String role;
	
	public CourseUserRoleBean(String courseInstanceID, String userJMBAG,
			String role) {
		super();
		this.courseInstanceID = courseInstanceID;
		this.userJMBAG = userJMBAG;
		this.role = role;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public String getUserJMBAG() {
		return userJMBAG;
	}

	public String getRole() {
		return role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((courseInstanceID == null) ? 0 : courseInstanceID.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result
				+ ((userJMBAG == null) ? 0 : userJMBAG.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CourseUserRoleBean other = (CourseUserRoleBean) obj;
		if (courseInstanceID == null) {
			if (other.courseInstanceID != null)
				return false;
		} else if (!courseInstanceID.equals(other.courseInstanceID))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (userJMBAG == null) {
			if (other.userJMBAG != null)
				return false;
		} else if (!userJMBAG.equals(other.userJMBAG))
			return false;
		return true;
	}
}
