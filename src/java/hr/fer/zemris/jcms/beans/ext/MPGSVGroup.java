package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.util.StringUtil;

public class MPGSVGroup implements Comparable<MPGSVGroup> {

	private Long id;
	private String compositeCourseID;
	private String relativePath;
	private String name;
	private boolean managedRoot;
	private int capacity = -1;
	private boolean enteringAllowed = false;
	private boolean leavingAllowed = false;
	private String mpSecurityTag;
	
	public MPGSVGroup() {
	}

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
		this.mpSecurityTag = mpSecurityTag;
	}

	@Override
	public int compareTo(MPGSVGroup o) {
		return StringUtil.HR_COLLATOR.compare(this.getName(), o.getName());
	}
}
