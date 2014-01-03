package hr.fer.zemris.jcms.beans;

public class ApplicationDefinitionBean {
	private String id;
	private String name;
	private String shortName;
	private String openFrom;
	private String openUntil;
	private String courseInstanceID;
	private String program;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getOpenFrom() {
		return openFrom;
	}
	public void setOpenFrom(String openFrom) {
		this.openFrom = openFrom;
	}
	public String getOpenUntil() {
		return openUntil;
	}
	public void setOpenUntil(String openUntil) {
		this.openUntil = openUntil;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
}
