package hr.fer.zemris.jcms.applications.exceptions;

public class ApplDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApplDefinitionException() {
	}

	public ApplDefinitionException(String message) {
		super(message);
	}

	public ApplDefinitionException(Throwable cause) {
		super(cause);
	}

	public ApplDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

}
