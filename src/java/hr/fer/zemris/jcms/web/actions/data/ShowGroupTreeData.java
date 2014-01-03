package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.security.GroupPermissions;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.Tree;

public class ShowGroupTreeData extends BaseCourseInstance {

	private Group parent;
	private Tree<Group, GroupPermissions> accessibleGroupsTree;
	private List<Group> privateGroups;
	
	public ShowGroupTreeData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}

	public Tree<Group, GroupPermissions> getAccessibleGroupsTree() {
		return accessibleGroupsTree;
	}
	public void setAccessibleGroupsTree(Tree<Group, GroupPermissions> accessibleGroupsTree) {
		this.accessibleGroupsTree = accessibleGroupsTree;
	}
	
	public List<Group> getPrivateGroups() {
		return privateGroups;
	}
	public void setPrivateGroups(List<Group> privateGroups) {
		this.privateGroups = privateGroups;
	}
}
