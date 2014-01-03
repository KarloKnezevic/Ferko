package hr.fer.zemris.jcms.beans.ext;

public class ConfPreloadScoreBean extends BaseUserBean {
	public String score;
	public Long id;
	public String assigner;
	public String oScore;
	public long version;
	
	public ConfPreloadScoreBean() {
	}

	public String getScore() {
		return score;
	}
	public void setScore(String score) {
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
	
	public String getOScore() {
		return oScore;
	}
	public void setOScore(String score) {
		oScore = score;
	}
	
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
}
