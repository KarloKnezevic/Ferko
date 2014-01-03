package hr.fer.zemris.jcms.beans.ext;

public class UserAnswersBean {
	private String jmbag;
	private String[] answers;
	private String group;
	
	public UserAnswersBean(String jmbag, String group, int listSize) {
		super();
		this.jmbag = jmbag;
		this.answers = new String[listSize];
		this.group = group;
	}
	
	public UserAnswersBean() {
	}
	
	public void setValue(String val, int index) {
		if (this.answers == null) {
			throw new IllegalArgumentException(
					"Lista odgovora nije inicijalizirana!");
		}
		
		this.answers[index] = val;
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

	public String[] getAnswers() {
		return this.answers;
	}

	public void setAnswers(String[] answers) {
		this.answers = answers;
	}
	
	
}
