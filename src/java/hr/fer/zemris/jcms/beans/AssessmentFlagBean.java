package hr.fer.zemris.jcms.beans;

public class AssessmentFlagBean {

	private String id;
	private String name;
	private String shortName;
	private String assesmentFlagTagID;
	private String courseInstanceID;
	private String programType;
	private String program;
	private int programVersion;
	private String visibility;
	private int sortIndex;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
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
	public String getAssesmentFlagTagID() {
		return assesmentFlagTagID;
	}
	public void setAssesmentFlagTagID(String assesmentFlagTagID) {
		this.assesmentFlagTagID = assesmentFlagTagID;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	public String getProgramType() {
		return programType;
	}
	public void setProgramType(String programType) {
		this.programType = programType;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public int getProgramVersion() {
		return programVersion;
	}
	public void setProgramVersion(int programVersion) {
		this.programVersion = programVersion;
	}
	
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public int getSortIndex() {
		return sortIndex;
	}
	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
}
