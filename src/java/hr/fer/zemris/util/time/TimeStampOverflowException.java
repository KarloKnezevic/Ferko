package hr.fer.zemris.util.time;

public class TimeStampOverflowException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TimeStampOverflowException() {
	}

	public TimeStampOverflowException(String message) {
		super(message);
	}

	public TimeStampOverflowException(Throwable cause) {
		super(cause);
	}

	public TimeStampOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

}
