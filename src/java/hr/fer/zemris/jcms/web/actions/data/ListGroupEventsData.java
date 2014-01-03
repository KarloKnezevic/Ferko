package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class ListGroupEventsData extends BaseGroup {

	private List<GroupOwner> owners;
	private List<GroupWideEvent> events;
	private Long groupID;
	private GroupSupportedPermission perm;
	
	public ListGroupEventsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<GroupWideEvent> getEvents() {
		return events;
	}
	public void setEvents(List<GroupWideEvent> events) {
		this.events = events;
	}
	
	public List<GroupOwner> getOwners() {
		return owners;
	}
	public void setOwners(List<GroupOwner> owners) {
		this.owners = owners;
	}
	
	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
	
	public GroupSupportedPermission getPerm() {
		return perm;
	}
	public void setPerm(GroupSupportedPermission perm) {
		this.perm = perm;
	}
}
