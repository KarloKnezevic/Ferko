package hr.fer.zemris.util.time;

public class TimeStampParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TimeStampParseException() {
	}

	public TimeStampParseException(String message) {
		super(message);
	}

	public TimeStampParseException(Throwable cause) {
		super(cause);
	}

	public TimeStampParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
