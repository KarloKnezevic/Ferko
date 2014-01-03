package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.MPRootInfoBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class MPGroupsListData extends BaseCourseInstance {

	private List<MPRootInfoBean> mpRoots;
	private String courseInstanceID;
	private boolean student;
	
	public MPGroupsListData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<MPRootInfoBean> getMpRoots() {
		return mpRoots;
	}
	public void setMpRoots(List<MPRootInfoBean> mpRoots) {
		this.mpRoots = mpRoots;
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public boolean getStudent() {
		return student;
	}
    public void setStudent(boolean student) {
		this.student = student;
	}
}
