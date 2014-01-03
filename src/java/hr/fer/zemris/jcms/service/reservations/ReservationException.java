package hr.fer.zemris.jcms.service.reservations;

public class ReservationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReservationException() {
	}

	public ReservationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReservationException(String message) {
		super(message);
	}

	public ReservationException(Throwable cause) {
		super(cause);
	}
}
