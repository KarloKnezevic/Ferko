package hr.fer.zemris.jcms.applications.exceptions;

public class ApplDuplicateOptionException extends ApplDefinitionException {

	private static final long serialVersionUID = 1L;

	private String name;
	private String key;
	
	public ApplDuplicateOptionException(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public ApplDuplicateOptionException(String name, String key, String message) {
		super(message);
		this.name = name;
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}
}
