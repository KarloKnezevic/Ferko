package hr.fer.zemris.jcms.applications.exceptions;

public class ApplDuplicateNameException extends ApplDefinitionException {

	private static final long serialVersionUID = 1L;

	private String name;
	
	public ApplDuplicateNameException(String name) {
		this.name = name;
	}

	public ApplDuplicateNameException(String name, String message) {
		super(message);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
