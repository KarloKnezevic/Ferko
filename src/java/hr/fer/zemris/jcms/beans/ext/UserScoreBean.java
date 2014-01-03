package hr.fer.zemris.jcms.beans.ext;

public class UserScoreBean {
	private String jmbag;
	private String value;
	private Double doubleValue;
	
	public UserScoreBean(String jmbag, String value, Double doubleValue) {
		super();
		this.jmbag = jmbag;
		this.value = value;
		this.doubleValue = doubleValue;
	}
	
	public UserScoreBean() {
	}

	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public Double getDoubleValue() {
		return doubleValue;
	}
	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}
}
