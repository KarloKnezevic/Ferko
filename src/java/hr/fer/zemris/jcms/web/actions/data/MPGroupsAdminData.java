package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.MarketPlaceBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.service.has.HasParent;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class MPGroupsAdminData extends BaseCourseInstance implements HasParent {

	private Group parent;
	private boolean active;
	private String courseInstanceID;
	private Long parentID;
	
	private	MarketPlaceBean bean = new MarketPlaceBean();
	
	public MPGroupsAdminData(IMessageLogger messageLogger) {
		super(messageLogger);
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

    public MarketPlaceBean getBean() {
		return bean;
	}
    public void setBean(MarketPlaceBean bean) {
		this.bean = bean;
	}

}
