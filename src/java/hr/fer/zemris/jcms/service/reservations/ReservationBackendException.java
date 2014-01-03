package hr.fer.zemris.jcms.service.reservations;

/**
 * Ova iznimka bit će bačena ako je problem kod rezervacija nastupio
 * s konkretnom implementacijom sustava koji nudi uslugu rezervacije
 * (primjerice, komuniciramo s njim preko tcp-a i pukne veza, ili sam
 * sustav vrati nešto neočekivano). Ova vrsta iznimke dodana je kako
 * bi se moglo razlikovati potencijalno privremene pogreške kod kojih
 * ima smisla pokušati opet nakon nekog vremena, i stalne pogreške.
 *  
 * @author marcupic
 */
public class ReservationBackendException extends ReservationException {

	private static final long serialVersionUID = 1L;

	public ReservationBackendException() {
		super();
	}

	public ReservationBackendException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReservationBackendException(String message) {
		super(message);
	}

	public ReservationBackendException(Throwable cause) {
		super(cause);
	}
}
