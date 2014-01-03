package hr.fer.zemris.jcms.service.assessments;

public class CalculationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CalculationException() {
	}

	public CalculationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CalculationException(String message) {
		super(message);
	}
}
