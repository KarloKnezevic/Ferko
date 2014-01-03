package hr.fer.zemris.jcms.beans.ext;

public class GroupFlat {
	private String isvuCode;
	private String groupName;
	private String compositeCourseID;
	private String relativePath;
	
	public GroupFlat() {
	}

	public String getIsvuCode() {
		return isvuCode;
	}

	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
}
