package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.beans.ext.BaseUserBean;

import java.util.Collections;
import java.util.List;

public class GroupBean {
	
	private Long id;
	private String compositeCourseID;
	private String relativePath;
	private String name;
	private boolean managedRoot;
	private int capacity = -1;
	private boolean enteringAllowed;
	private boolean leavingAllowed;
	private String mpSecurityTag;
	private List<BaseUserBean> ownerList = Collections.emptyList();

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCompositeCourseID() {
		return compositeCourseID;
	}
	public void setCompositeCourseID(String compositeCourseID) {
		this.compositeCourseID = compositeCourseID;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isManagedRoot() {
		return managedRoot;
	}
	public void setManagedRoot(boolean managedRoot) {
		this.managedRoot = managedRoot;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public boolean isEnteringAllowed() {
		return enteringAllowed;
	}
	public void setEnteringAllowed(boolean enteringAllowed) {
		this.enteringAllowed = enteringAllowed;
	}
	public boolean isLeavingAllowed() {
		return leavingAllowed;
	}
	public void setLeavingAllowed(boolean leavingAllowed) {
		this.leavingAllowed = leavingAllowed;
	}
	public String getMpSecurityTag() {
		return mpSecurityTag;
	}
	public void setMpSecurityTag(String mpSecurityTag) {
		if(mpSecurityTag!=null && mpSecurityTag.trim().length()==0) {
			this.mpSecurityTag = null;
		} else {
			this.mpSecurityTag = mpSecurityTag;
		}
	}
	public List<BaseUserBean> getOwnerList() {
		return ownerList;
	}
	public void setOwnerList(List<BaseUserBean> ownerList) {
		this.ownerList = ownerList;
	}
}
