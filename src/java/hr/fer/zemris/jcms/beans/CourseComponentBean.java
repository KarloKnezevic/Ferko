package hr.fer.zemris.jcms.beans;

public class CourseComponentBean {
	
	private String id;
	private String courseInstanceID;
	private String shortName;
	private String name;
	
	public CourseComponentBean() {
	}

	public CourseComponentBean(Long id, String name){
		this.id = Long.toString(id);
		this.name=name;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
