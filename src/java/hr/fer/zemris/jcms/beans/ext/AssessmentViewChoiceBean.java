package hr.fer.zemris.jcms.beans.ext;

public class AssessmentViewChoiceBean extends AssessmentViewConfDataBean {
	private int numberOfProblems;
	private String[] answers;
	private String[] answersStatus;
	private String[] correctAnswers;
	private double scoreCorrect;
	private double scoreIncorrect;
	private double scoreUnanswered;
	private String[] problemsLabels;
	private String group;
	private boolean usingDetailedTaskScores;
	private String[] detailedScoresCorrect;
	private String[] detailedScoresIncorrect;
	private String[] detailedScoresUnanswered;
	
	public String[] getDetailedScoresCorrect() {
		return detailedScoresCorrect;
	}

	public void setDetailedScoresCorrect(String[] detailedScoresCorrect) {
		this.detailedScoresCorrect = detailedScoresCorrect;
	}

	public String[] getDetailedScoresIncorrect() {
		return detailedScoresIncorrect;
	}

	public void setDetailedScoresIncorrect(String[] detailedScoresIncorrect) {
		this.detailedScoresIncorrect = detailedScoresIncorrect;
	}

	public String[] getDetailedScoresUnanswered() {
		return detailedScoresUnanswered;
	}

	public void setDetailedScoresUnanswered(String[] detailedScoresUnanswered) {
		this.detailedScoresUnanswered = detailedScoresUnanswered;
	}

	public AssessmentViewChoiceBean() {
	}
	
	public String getProblemsLabels(int index) {
		if (this.problemsLabels == null) {
			this.problemsLabels = new String[numberOfProblems];
		}
		return this.problemsLabels[index];
	}
	
	public void setProblemsLabels(int index, String problemsLabels) {
		if (this.problemsLabels == null) {
			this.problemsLabels = new String[numberOfProblems];
		}
		this.problemsLabels[index] = problemsLabels;
	}
	
	public String getAnswers(int index) {
		if (this.answers == null) {
			this.answers = new String[numberOfProblems];
		}
		return this.answers[index];
	}
	
	public void setAnswers(int index, String answer) {
		if (this.answers == null) {
			this.answers = new String[numberOfProblems];
		}
		this.answers[index] = answer;
	}
	
	public String getCorrectAnswers(int index) {
		if (this.correctAnswers == null) {
			this.correctAnswers = new String[numberOfProblems];
		}
		return this.correctAnswers[index];
	}
	
	public void setCorrectAnswers(int index, String correctAnswer) {
		if (this.correctAnswers == null) {
			this.correctAnswers = new String[numberOfProblems];
		}
		this.correctAnswers[index] = correctAnswer;
	}
	
	public String getAnswersStatus(int index) {
		if (this.answersStatus == null) {
			this.answersStatus = new String[numberOfProblems];
		}
		return this.answersStatus[index];
	}
	
	public void setAnswersStatus(int index, String answerStatus) {
		if (this.answersStatus == null) {
			this.answersStatus = new String[numberOfProblems];
		}
		this.answersStatus[index] = answerStatus;
	}
	
	
	public int getNumberOfProblems() {
		return numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}



	public String[] getAnswers() {
		return answers;
	}



	public void setAnswers(String[] answers) {
		this.answers = answers;
	}



	public String[] getAnswersStatus() {
		return answersStatus;
	}



	public void setAnswersStatus(String[] answersStatus) {
		this.answersStatus = answersStatus;
	}



	public String[] getCorrectAnswers() {
		return correctAnswers;
	}



	public void setCorrectAnswers(String[] correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	public double getScoreCorrect() {
		return scoreCorrect;
	}

	public void setScoreCorrect(double scoreCorrect) {
		this.scoreCorrect = scoreCorrect;
	}

	public double getScoreIncorrect() {
		return scoreIncorrect;
	}

	public void setScoreIncorrect(double scoreIncorrect) {
		this.scoreIncorrect = scoreIncorrect;
	}

	public double getScoreUnanswered() {
		return scoreUnanswered;
	}

	public void setScoreUnanswered(double scoreUnanswered) {
		this.scoreUnanswered = scoreUnanswered;
	}

	public String[] getProblemsLabels() {
		return problemsLabels;
	}

	public void setProblemsLabels(String[] problemsLabels) {
		this.problemsLabels = problemsLabels;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean getUsingDetailedTaskScores() {
		return usingDetailedTaskScores;
	}

	public void setUsingDetailedTaskScores(boolean usingDetailedTaskScores) {
		this.usingDetailedTaskScores = usingDetailedTaskScores;
	}
}
