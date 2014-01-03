package hr.fer.zemris.jcms.service.reservations;

import java.util.List;

/**
 * <p>Sucelje koje opisuje jedan konkretan sustav rezervacija. Jednom stvoren, ovaj objekt smije se koristiti do poziva metode {@link #close()}
 * koju je <b>obavezno</b> pozvati na samom kraju kako bi se oslobodili eventualno zauzeti resursi.</p>
 * 
 * <p>Ove objekte stvara {@link IReservationManagerFactory}.</p>
 * 
 * @author marcupic
 *
 */
public interface IReservationManager {
	
	/**
	 * Provjerava je li dvorana pod kontrolom ovog managera.
	 * @param roomShortName kratki naziv dvorane
	 * @return {@linkplain Boolean#TRUE} ako je, {@linkplain Boolean#FALSE} ako nije, te <code>null</code> ako se je dogodila pogreška.  
	 */
	public Boolean isUnderControl(String roomShortName) throws ReservationException;
	
	/**
	 * Ova metoda zatvara room manager i eventualne veze prema vanjskim resursima, i nakon toga se vise ne smije koristiti.
	 * Korisnik je DUZAN pozvati ovu metodu.
	 */
	public void close() throws ReservationException;
	
	/**
	 * Rezervira dvoranu u zadanom terminu.
	 * @param room kratko ime sobe
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason razlog rezervacije
	 * @return <code>true</code> ako je rezervacija uspjela; <code>false</code> ako nije uspjela.
	 * @throws ReservationException u slucaju greske, ili u slucaju neuspjele rezervacije (konflikta) - jos istrazi ovaj drugi slucaj.
	 */
	public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException;

	/**
	 * Rezervira dvoranu u zadanom terminu.
	 * @param room kratko ime sobe
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason razlog rezervacije
	 * @param context kontekst koji se predaje dalje bazi dvorana
	 * @return <code>true</code> ako je rezervacija uspjela; <code>false</code> ako nije uspjela.
	 * @throws ReservationException u slucaju greske, ili u slucaju neuspjele rezervacije (konflikta) - jos istrazi ovaj drugi slucaj.
	 */
	public boolean allocateRoom(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException;

	/**
	 * Rezervira dvorane u zadanom terminu. Po povratku modificira zadane objekte tako da evidentira sto je uspjesno zauzeto a sto ne.
	 * U slucaju uspjeha, pozvat ce {@link RoomReservationTask#setSuccess(boolean)} s argumentom <code>true</code>. Inace ce predati <code>false</code>.
	 * @param rooms lista objekata koji opisuju sto treba rezervirati
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason razlog rezervacije
	 * @return <code>true</code> ako su sve rezervacije uspjele; <code>false</code> ako barem jedna nije.
	 * @throws ReservationException u slucaju greske, ili u slucaju neuspjele rezervacije (konflikta) - jos istrazi ovaj drugi slucaj.
	 */
	public boolean allocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException;
	
	/**
	 * Provjeri stanje rezervacije dvorane.
	 * 
	 * @param room kratko ime prostorije
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason očekivani razlog rezervacije
	 * @return 0 ako je slobodna, 1 ako je vec naša, 2 ako je zauzeta nečim drugim
	 * @throws ReservationException
	 */
	public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException;

	/**
	 * Provjeri stanje rezervacije dvorane.
	 * 
	 * @param room kratko ime prostorije
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason očekivani razlog rezervacije
	 * @param context očekivani kontekst; ako ga sustav rezervacija vrati za sobu, onda znamo da je naša
	 * @return 0 ako je slobodna, 1 ako je vec naša, 2 ako je zauzeta nečim drugim
	 * @throws ReservationException
	 */
	public RoomReservation checkRoom(String room, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException;

	/**
	 * Provjeri stanje rezervacija zadanih dvorana i status direktno podesava u predanim objektima.
	 * 
	 * @param rooms lista soba kojima treba provjeriti status
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason
	 * @return 0 ako je slobodna, 1 ako je vec naša, 2 ako je zauzeta nečim drugim
	 * @throws ReservationException
	 */
	public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason) throws ReservationException;

	/**
	 * Provjeri stanje rezervacija zadanih dvorana i status direktno podesava u predanim objektima.
	 * 
	 * @param rooms lista soba kojima treba provjeriti status
	 * @param dateTimeFrom datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @param reason
	 * @param context očekivani kontekst; ako ga sustav rezervacija vrati za sobu, onda znamo da je naša
	 * @return 0 ako je slobodna, 1 ako je vec naša, 2 ako je zauzeta nečim drugim
	 * @throws ReservationException
	 */
	public void checkRoom(List<RoomReservation> rooms, String dateTimeFrom, String dateTimeTo, String reason, String context) throws ReservationException;
	
	/**
	 * Metoda koja za listu soba i zadani vremenski period vraća sve rezervacije koje postoje u sustavu rezervacija.
	 * 
	 * @param rooms lista kratkih imena soba
	 * @param dateTimeFrom datum pocetka (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja (yyyy-MM-dd HH:mm)
	 * @return listu rezervacija
	 * @throws ReservationException
	 */
	public List<RoomReservationEntry> listRoomReservations(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException;
	
	/**
	 * Dohvaca slobodne periode zadane dvorane u zadanom periodu
	 * @param room kratko ime prostorije
	 * @param dateTimeFrom  datum pocetka perioda (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo  datum zavrsetka perioda (yyyy-MM-dd HH:mm)
	 * @return
	 * @throws ReservationException
	 */
	public List<RoomReservationPeriod> findAvailableRoomPeriods(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException;
	
	/**
	 * Dohvaca slobodne periode zadanih dvorana u zadanom periodu
	 * @param rooms  popis kratkih imena prostorija
	 * @param dateTimeFrom  datum pocetka perioda (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo  datum zavrsetka perioda (yyyy-MM-dd HH:mm)
	 * @return
	 * @throws ReservationException
	 */
	public List<RoomReservationPeriod> findAvailablePeriodsForRooms(List<String> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException;

	/**
	 * Oslobađa rezerviranu dvoranu.
	 * @param room kratko ime prostorije
	 * @param dateTimeFrom  datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @return true ako je uspjelo, false inace
	 * @throws ReservationException u slucaju pogreske
	 */
	public boolean deallocateRoom(String room, String dateTimeFrom, String dateTimeTo) throws ReservationException;

	/**
	 * Oslobada rezervirane dvorane u zadanom terminu. Po povratku modificira zadane objekte tako da evidentira sto je uspjesno zauzeto a sto ne.
	 * U slucaju uspjeha, pozvat ce {@link RoomReservationTask#setSuccess(boolean)} s argumentom <code>true</code>. Inace ce predati <code>false</code>.
	 * @param rooms lista objekata koji opisuju sto treba odrezervirati
	 * @param dateTimeFrom  datum pocetka rezervacije (yyyy-MM-dd HH:mm)
	 * @param dateTimeTo datum kraja rezervacije (yyyy-MM-dd HH:mm)
	 * @return true ako je uspjelo, false inace
	 * @throws ReservationException u slucaju pogreske
	 */
	public boolean deallocateRooms(List<RoomReservationTask> rooms, String dateTimeFrom, String dateTimeTo) throws ReservationException;

	/**
	 * Metoda mijenja trajanje postojeće rezervacije (dozvoljeno je mijenjati i početak i trajanje).
	 * @param room
	 * @param oldDateTimeFrom
	 * @param oldDateTimeTo
	 * @param newDateTimeFrom
	 * @param newDateTimeTo
	 * @param reason
	 * @param context
	 * @param justCheck ako je <code>true</code>, promjena se nece obaviti, vec ce se provjeriti je li je moguce napraviti 
	 * @return <code>true</code> ako je promjena uspjela, <code>false</code> inače
	 */
	public boolean updateReservationRoom(String room, String oldDateTimeFrom,
			String oldDateTimeTo, String newDateTimeFrom, String newDateTimeTo, String reason,
			String context, boolean justCheck) throws ReservationException;
}
