package hr.fer.zemris.jcms.applications.exceptions;

public class ApplMissingTextException extends ApplDefinitionException {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ApplMissingTextException(String name) {
		this.name = name;
	}

	public ApplMissingTextException(String name, String message) {
		super(message);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
