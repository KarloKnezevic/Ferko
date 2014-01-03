package hr.fer.zemris.jcms.beans;

public class KeyValueBean {
	
	private String key;
	private String value;

	public KeyValueBean() {
	}
	
	public KeyValueBean(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
