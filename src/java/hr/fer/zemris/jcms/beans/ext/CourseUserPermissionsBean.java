package hr.fer.zemris.jcms.beans.ext;

import java.util.List;

public class CourseUserPermissionsBean {

	private List<CourseUserPermissions> userPermissions;
	private CourseUserPermissions newUser;
	
	public List<CourseUserPermissions> getUserPermissions() {
		return userPermissions;
	}
	public void setUserPermissions(List<CourseUserPermissions> userPermissions) {
		this.userPermissions = userPermissions;
	}

	public CourseUserPermissions getNewUser() {
		if(newUser==null) {
			newUser = new CourseUserPermissions();
		}
		return newUser;
	}
	public void setNewUser(CourseUserPermissions newUser) {
		this.newUser = newUser;
	}
}
