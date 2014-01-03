package hr.fer.zemris.jcms.beans.ext;

public class GroupMembershipExportBean {
	private String semesterID;  // postavit ce se ili semesterID ili courseInstanceID
	private String courseInstanceID;
	private String parentRelativePath; // postavit ce se ili parentRelativePath ili relativePath
	private String relativePath;
	private String format;
	private boolean writeStudentTag;
	private boolean writeStudentName;
	private boolean writeISVUCode;
	
	public String getSemesterID() {
		return semesterID;
	}
	public void setSemesterID(String semesterID) {
		this.semesterID = semesterID;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	public String getParentRelativePath() {
		return parentRelativePath;
	}
	public void setParentRelativePath(String parentRelativePath) {
		this.parentRelativePath = parentRelativePath;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public boolean getWriteStudentTag() {
		return writeStudentTag;
	}
	public void setWriteStudentTag(boolean writeStudentTag) {
		this.writeStudentTag = writeStudentTag;
	}
	public boolean getWriteStudentName() {
		return writeStudentName;
	}
	public void setWriteStudentName(boolean writeStudentName) {
		this.writeStudentName = writeStudentName;
	}
	public boolean getWriteISVUCode() {
		return writeISVUCode;
	}
	public void setWriteISVUCode(boolean writeISVUCode) {
		this.writeISVUCode = writeISVUCode;
	}
}
