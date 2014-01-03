package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.Tree;

public class ShowGroupTree2Data extends BaseCourseInstance {

	private Group parent;
	private String courseInstanceID;
	private Tree<Group, GroupSupportedPermission> accessibleGroupsTree;
	private List<Group> privateGroups;
	private String treeAsJSON;
	private Long parentID;
	private boolean allowMultipleAddition;
	private String text;
	
	public ShowGroupTree2Data(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Long getParentID() {
		return parentID;
	}
	public void setParentID(Long parentID) {
		this.parentID = parentID;
	}
	public boolean getAllowMultipleAddition() {
		return allowMultipleAddition;
	}
	public void setAllowMultipleAddition(boolean allowMultipleAddition) {
		this.allowMultipleAddition = allowMultipleAddition;
	}
	
	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}

	public Tree<Group, GroupSupportedPermission> getAccessibleGroupsTree() {
		return accessibleGroupsTree;
	}
	public void setAccessibleGroupsTree(Tree<Group, GroupSupportedPermission> accessibleGroupsTree) {
		this.accessibleGroupsTree = accessibleGroupsTree;
	}
	
	public List<Group> getPrivateGroups() {
		return privateGroups;
	}
	public void setPrivateGroups(List<Group> privateGroups) {
		this.privateGroups = privateGroups;
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public String getTreeAsJSON() {
		return treeAsJSON;
	}
    public void setTreeAsJSON(String treeAsJSON) {
		this.treeAsJSON = treeAsJSON;
	}
}
