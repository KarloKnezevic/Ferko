package hr.fer.zemris.jcms.beans.ext;

public class ConfProblemsScoreBean extends BaseUserBean {
	private String[] score;
	private String[] oldScore;
	private Long id;
	private String assigner;
	private boolean present;
	private boolean oPresent;
	private int numberOfProblems;
	private long version;
	private String group;
	private String oGroup;
	
	public ConfProblemsScoreBean() {
	}
	
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public String getOGroup() {
		return oGroup;
	}
	public void setOGroup(String group) {
		oGroup = group;
	}
	
	public String getOldScore(int index) {
		if (this.oldScore == null) {
			this.oldScore = new String[numberOfProblems];
		}
		return this.oldScore[index];
	}
	
	public void setOldScore(int index, String oldScore) {
		if (this.oldScore == null) {
			this.oldScore = new String[numberOfProblems];
		}
		this.oldScore[index] = oldScore;
	}

	public String[] getOldScore() {
		return oldScore;
	}
	public void setOldScore(String[] oldScore) {
		this.oldScore = oldScore;
	}
	
	public String getScore(int index) {
		if (this.score == null) {
			this.score = new String[numberOfProblems];
		}
		return this.score[index];
	}
	
	public void setScore(int index, String score) {
		if (this.score == null) {
			this.score = new String[numberOfProblems];
		}
		this.score[index] = score;
	}

	public String[] getScore() {
		return score;
	}
	public void setScore(String[] score) {
		this.score = score;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getAssigner() {
		return assigner;
	}
	public void setAssigner(String assigner) {
		this.assigner = assigner;
	}

	public boolean getPresent() {
		return this.present;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}

	public boolean getOPresent() {
		return this.oPresent;
	}

	public void setOPresent(boolean oPresent) {
		this.oPresent = oPresent;
	}

	public int getNumberOfProblems() {
		return this.numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}
}
