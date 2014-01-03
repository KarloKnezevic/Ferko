package hr.fer.zemris.jcms.service.reservations;

/**
 * Status rezerviranosti prostorije.
 * 
 * @author marcupic
 *
 */
public enum RoomReservationStatus {
	/**
	 * Prostorija je slobodna 
	 */
	FREE,
	/**
	 * Prostorija je rezervirana, i to za nas 
	 */
	RESERVED_FOR_US,
	/**
	 * Prostorija je rezervirana, ali za nekog drugog (ovo je u principu konflikt) 
	 */
	RESERVED_FOR_OTHER,
	/**
	 * Prostorija nije pod kontrolom sustava za rezervaciju 
	 */
	NOT_UNDER_CONTROL
}
