package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

public class GroupOwnershipBean {
	private List<GroupOwnershipsBean> users = new ArrayList<GroupOwnershipsBean>();
	private String courseInstanceID;

	public GroupOwnershipBean() {
	}

	public List<GroupOwnershipsBean> getUsers() {
		return users;
	}

	public void setUsers(List<GroupOwnershipsBean> users) {
		this.users = users;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
