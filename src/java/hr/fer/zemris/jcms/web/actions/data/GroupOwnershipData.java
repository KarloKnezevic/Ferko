package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class GroupOwnershipData extends BaseCourseInstance {

	private List<Group> allGroups;
	
	public GroupOwnershipData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<Group> getAllGroups() {
		return allGroups;
	}
	public void setAllGroups(List<Group> allGroups) {
		this.allGroups = allGroups;
	}
}
