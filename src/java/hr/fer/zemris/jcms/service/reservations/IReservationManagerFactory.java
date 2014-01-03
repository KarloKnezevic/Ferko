package hr.fer.zemris.jcms.service.reservations;

public interface IReservationManagerFactory {
	/**
	 * Za korisnika čiji se predaje userID, jmbag i username stvara jedan {@link IReservationManager}.
	 * @param userID id korisnika iz ferka
	 * @param jmbag jmbag korisnika
	 * @param username username korisnika
	 * @return manager
	 */
	public IReservationManager getInstance(Long userID, String jmbag, String username) throws ReservationException;
}
