package hr.fer.zemris.jcms.beans;

/**
 * Ovo su podaci o jednom primjerku vanjskog ispita za jednog studenta.
 * 
 * @author marcupic
 */
public class TestInstanceDataBean {
	private String id;
	private String title;
	private String description;
	private String testStatus;
	private double testScore;
	private int previousAttempts;
	private String startedAt;
	private String finishedAt;
	private String url;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return Returns the previousAttempts.
	 */
	public int getPreviousAttempts() {
		return previousAttempts;
	}
	/**
	 * @param previousAttempts The previousAttempts to set.
	 */
	public void setPreviousAttempts(int previousAttempts) {
		this.previousAttempts = previousAttempts;
	}
	/**
	 * @return Returns the testScore.
	 */
	public double getTestScore() {
		return testScore;
	}
	/**
	 * @param testScore The testScore to set.
	 */
	public void setTestScore(double testScore) {
		this.testScore = testScore;
	}
	/**
	 * @return Returns the testStatus.
	 */
	public String getTestStatus() {
		return testStatus;
	}
	/**
	 * @param testStatus The testStatus to set.
	 */
	public void setTestStatus(String testStatus) {
		this.testStatus = testStatus;
	}
	
	public String getFinishedAt() {
		return finishedAt;
	}
	public void setFinishedAt(String finishedAt) {
		this.finishedAt = finishedAt;
	}
	public String getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(String startedAt) {
		this.startedAt = startedAt;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getId()).append(", ").append(getTitle()).append(", ").append(getTestStatus()).append(", ").append(getTestScore());
		return sb.toString();
	} 
}
