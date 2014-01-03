package hr.fer.zemris.jcms.beans.ext;

public class JMBAGLoginBean {
	private String jmbag;
	private String username;
	
	public JMBAGLoginBean() {
	}

	public JMBAGLoginBean(String jmbag, String username) {
		super();
		this.jmbag = jmbag;
		this.username = username;
	}

	public String getJmbag() {
		return jmbag;
	}

	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
