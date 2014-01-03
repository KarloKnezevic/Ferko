package hr.fer.zemris.jcms.beans;


public class CourseComponentItemBean {
	
	private String id;
	private String courseComponentID;
	private String position;
	private String name;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCourseComponentID() {
		return courseComponentID;
	}
	public void setCourseComponentID(String courseComponentID) {
		this.courseComponentID = courseComponentID;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
