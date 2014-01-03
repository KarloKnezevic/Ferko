package hr.fer.zemris.jcms.beans.ext;

import java.util.List;

public class ComponentTaskAssignmentBean extends BaseUserBean {
	
	private String id;
	private boolean locked;
	private String lockingDate;
	private List<TaskFileBean> fileList;

	public ComponentTaskAssignmentBean() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<TaskFileBean> getFileList() {
		return fileList;
	}

	public void setFileList(List<TaskFileBean> fileList) {
		this.fileList = fileList;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getLockingDate() {
		return lockingDate;
	}

	public void setLockingDate(String lockingDate) {
		this.lockingDate = lockingDate;
	}
}
