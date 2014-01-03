package hr.fer.zemris.jcms.beans.ext;

import java.util.Set;

public class GroupOwnershipsBean extends UserPartialBean {
	private Long id;
	private Set<Long> groups;
	
	public GroupOwnershipsBean() {
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Set<Long> getGroups() {
		return groups;
	}
	public void setGroups(Set<Long> groups) {
		this.groups = groups;
	}
}
