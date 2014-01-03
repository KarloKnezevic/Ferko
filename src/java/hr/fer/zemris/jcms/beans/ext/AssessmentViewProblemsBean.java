package hr.fer.zemris.jcms.beans.ext;

public class AssessmentViewProblemsBean extends AssessmentViewConfDataBean {
	private Double[] scores;
	private Double[] maxScores;
	private int numberOfProblems;
	private String group;
	
	public AssessmentViewProblemsBean() {
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public Double getScores(int index) {
		if (this.scores == null) {
			this.scores = new Double[numberOfProblems];
		}
		return this.scores[index];
	}
	
	public void setScore(int index, Double score) {
		if (this.scores == null) {
			this.scores = new Double[numberOfProblems];
		}
		this.scores[index] = score;
	}
	
	public Double[] getScores() {
		return scores;
	}

	public void setScores(Double[] scores) {
		this.scores = scores;
	}

	public Double getMaxScores(int index) {
		if (this.maxScores == null) {
			this.maxScores = new Double[numberOfProblems];
		}
		return this.maxScores[index];
	}
	
	public void setMaxScore(int index, Double maxScore) {
		if (this.maxScores == null) {
			this.maxScores = new Double[numberOfProblems];
		}
		this.maxScores[index] = maxScore;
	}

	public Double[] getMaxScores() {
		return maxScores;
	}
	
	public void setMaxScores(Double[] maxScores) {
		this.maxScores = maxScores;
	}
	
	public int getNumberOfProblems() {
		return numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}
}
