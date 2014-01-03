package hr.fer.zemris.jcms.beans.ext;

public class UserFlagValueBean {
	private String jmbag;
	private boolean value;
	public UserFlagValueBean(String jmbag, boolean value) {
		super();
		this.jmbag = jmbag;
		this.value = value;
	}
	
	public UserFlagValueBean() {
	}

	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public boolean getValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
}
