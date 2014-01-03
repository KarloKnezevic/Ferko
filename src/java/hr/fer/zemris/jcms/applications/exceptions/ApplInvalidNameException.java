package hr.fer.zemris.jcms.applications.exceptions;

public class ApplInvalidNameException extends ApplDefinitionException {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ApplInvalidNameException(String name) {
		this.name = name;
	}

	public ApplInvalidNameException(String name, String message) {
		super(message);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
