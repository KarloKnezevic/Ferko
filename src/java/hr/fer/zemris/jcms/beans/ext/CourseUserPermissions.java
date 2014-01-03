package hr.fer.zemris.jcms.beans.ext;

import java.util.HashSet;
import java.util.Set;

public class CourseUserPermissions extends UserPartialBean {
	private Long id;
	private Set<String> groupRelativePaths = new HashSet<String>();
	
	public CourseUserPermissions() {
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Set<String> getGroupRelativePaths() {
		return groupRelativePaths;
	}
	public void setGroupRelativePaths(Set<String> groupRelativePaths) {
		this.groupRelativePaths = groupRelativePaths;
	}
}
