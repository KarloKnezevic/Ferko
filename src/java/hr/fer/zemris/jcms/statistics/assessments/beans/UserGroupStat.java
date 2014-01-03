package hr.fer.zemris.jcms.statistics.assessments.beans;

public class UserGroupStat {

	Long userID;
	Long groupID;
	double score;
	
	public UserGroupStat() {
	}
	
	public UserGroupStat(Long userID, Long groupID) {
		super();
		this.userID = userID;
		this.groupID = groupID;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public Long getGroupID() {
		return groupID;
	}

	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
}
