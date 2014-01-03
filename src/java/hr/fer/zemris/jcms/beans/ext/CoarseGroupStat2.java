package hr.fer.zemris.jcms.beans.ext;

public class CoarseGroupStat2 {
	long groupID;
	String groupName;
	String compositeCourseID;
	String relativePath;
	long count;
	String courseName;
	String courseIsvuCode;
	int capacity;
	
	public CoarseGroupStat2(long groupID, String groupName, String compositeCourseID, String relativePath, int capacity, long count) {
		super();
		this.groupID = groupID;
		this.groupName = groupName;
		this.compositeCourseID = compositeCourseID;
		this.relativePath = relativePath;
		this.capacity = capacity;
		this.count = count;
	}

	public long getGroupID() {
		return groupID;
	}

	public void setGroupID(long groupID) {
		this.groupID = groupID;
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

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getCourseIsvuCode() {
		return courseIsvuCode;
	}

	public void setCourseIsvuCode(String courseIsvuCode) {
		this.courseIsvuCode = courseIsvuCode;
	}
	
}
