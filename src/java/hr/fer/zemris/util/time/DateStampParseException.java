package hr.fer.zemris.util.time;

public class DateStampParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DateStampParseException() {
	}

	public DateStampParseException(String message) {
		super(message);
	}

	public DateStampParseException(Throwable cause) {
		super(cause);
	}

	public DateStampParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
