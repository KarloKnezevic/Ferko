package hr.fer.zemris.jcms.beans.ext;

public class ConfChoiceScoreBean extends BaseUserBean {
	private String[] answers;
	private Long id;
	private String assigner;
	private String group;
	private boolean present;
	private int problemsNum;
	
	public ConfChoiceScoreBean() {
	}

	public String getZanswers(int index) {
		return getAnswers(index);
	}
	
	public String getAnswers(int index) {
		if (this.answers == null) {
			this.answers = new String[problemsNum];
		}
		return this.answers[index];
	}

	public void setZanswers(int index, String answer) {
		setAnswers(index, answer);
	}
	
	public void setAnswers(int index, String answer) {
		if (this.answers == null) {
			this.answers = new String[problemsNum];
		}
		this.answers[index] = answer;
	}

	public String[] getZanswers() {
		return getAnswers();
	}
	public String[] getAnswers() {
		return answers;
	}
	public void setZanswers(String[] answers) {
		setAnswers(answers);
	}
	public void setAnswers(String[] answers) {
		this.answers = answers;
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

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getProblemsNum() {
		return this.problemsNum;
	}

	public void setProblemsNum(int problemsNum) {
		this.problemsNum = problemsNum;
	}
}
