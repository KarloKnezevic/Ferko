package hr.fer.zemris.jcms.beans;

public class AssessmentBean {

	private String id;
	private String name;
	private String shortName;
	private String assesmentTagID;
	private String courseInstanceID;
	private String assesmentFlagID;
	private String programType;
	private String program;
	private String parentID;
	private String chainedParentID;
	private String maxScore;
	private int programVersion;
	private String startsAt;
	private String duration;
	private String visibility;
	private boolean locked;
	private int sortIndex;
	private boolean eventHidden;
	
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
	public String getAssesmentTagID() {
		return assesmentTagID;
	}
	public void setAssesmentTagID(String assesmentTagID) {
		this.assesmentTagID = assesmentTagID;
	}
	public String getAssesmentFlagID() {
		return assesmentFlagID;
	}
	public void setAssesmentFlagID(String assesmentFlagID) {
		this.assesmentFlagID = assesmentFlagID;
	}
	public String getParentID() {
		return parentID;
	}
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}
	public String getChainedParentID() {
		return chainedParentID;
	}
	public void setChainedParentID(String chainedParentID) {
		this.chainedParentID = chainedParentID;
	}
	public String getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(String maxScore) {
		this.maxScore = maxScore;
	}
	public String getStartsAt() {
		return startsAt;
	}
	public void setStartsAt(String startsAt) {
		this.startsAt = startsAt;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
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
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	public boolean getEventHidden() {
		return eventHidden;
	}
	public void setEventHidden(boolean eventHidden) {
		this.eventHidden = eventHidden;
	}
}
