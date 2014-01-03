package hr.fer.zemris.jcms.beans.ext;

public class ChoiceProblemMappingBean {
	private String groupLabel;
	private String problemLabel;
	private String type;
	private String version;
	
	public ChoiceProblemMappingBean() {
		
	}
	
	public ChoiceProblemMappingBean(String groupLabel, String problemLabel, String type, String version) {
		this.groupLabel = groupLabel;
		this.problemLabel = problemLabel;
		this.type = type;
		this.version = version;
	}

	public String getGroupLabel() {
		return groupLabel;
	}

	public void setGroupLabel(String groupLabel) {
		this.groupLabel = groupLabel;
	}

	public String getProblemLabel() {
		return problemLabel;
	}

	public void setProblemLabel(String problemLabel) {
		this.problemLabel = problemLabel;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
