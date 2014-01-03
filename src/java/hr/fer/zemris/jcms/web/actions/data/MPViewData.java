package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.MPUserState;
import hr.fer.zemris.jcms.beans.ext.MPViewBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.service.has.HasParent;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class MPViewData extends BaseCourseInstance implements HasParent {

	private Group parent;
	private boolean active;
	private MPUserState userState;
	private String now;
	private List<UserGroup> userGroups;
	private List<Group> allGroups;
	private String courseInstanceID;
	private Long parentID;
	private	MPViewBean bean = new MPViewBean();
	
	public MPViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public Long getParentID() {
		return parentID;
	}
    public void setParentID(Long parentID) {
		this.parentID = parentID;
	}

    public MPViewBean getBean() {
		return bean;
	}
    public void setBean(MPViewBean bean) {
		this.bean = bean;
	}

	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public MPUserState getUserState() {
		return userState;
	}
	public void setUserState(MPUserState userState) {
		this.userState = userState;
	}
	
	public String getNow() {
		return now;
	}
	public void setNow(String now) {
		this.now = now;
	}
	
	public List<UserGroup> getUserGroups() {
		return userGroups;
	}
	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}
	
	public List<Group> getAllGroups() {
		return allGroups;
	}
	public void setAllGroups(List<Group> allGroups) {
		this.allGroups = allGroups;
	}
}
