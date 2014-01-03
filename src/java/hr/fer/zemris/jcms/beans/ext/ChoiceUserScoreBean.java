package hr.fer.zemris.jcms.beans.ext;

public class ChoiceUserScoreBean {
	private String jmbag;
	private String answers;
	private String group;
	
	public ChoiceUserScoreBean(String jmbag, String group, String answers) {
		this.jmbag = jmbag;
		this.answers = answers;
		this.group = group;
	}
	
	public ChoiceUserScoreBean() {
	}
	
	public String getAnswers() {
		return this.answers;
	}

	public void setAnswers(String answers) {
		this.answers = answers;
	}

	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
}
