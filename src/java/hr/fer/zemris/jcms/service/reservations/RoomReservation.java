package hr.fer.zemris.jcms.service.reservations;

/**
 * Podaci o stanju rezerviranosti dvorane.
 * 
 * @author marcupic
 *
 */
public class RoomReservation {
	
	private String roomShortName;
	private RoomReservationStatus status;
	
	public RoomReservation(String roomShortName) {
		super();
		this.roomShortName = roomShortName;
	}
	
	public RoomReservation(String roomShortName, RoomReservationStatus status) {
		super();
		this.roomShortName = roomShortName;
		this.status = status;
	}

	/**
	 * Dohvaca status.
	 * @return status
	 */
	public RoomReservationStatus getStatus() {
		return status;
	}
	
	/**
	 * Postavlja status
	 * @param status status
	 */
	public void setStatus(RoomReservationStatus status) {
		this.status = status;
	}
	
	/**
	 * Vraca kratko ime prostorije
	 * @return kratko ime prostorije
	 */
	public String getRoomShortName() {
		return roomShortName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((roomShortName == null) ? 0 : roomShortName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoomReservation other = (RoomReservation) obj;
		if (roomShortName == null) {
			if (other.roomShortName != null)
				return false;
		} else if (!roomShortName.equals(other.roomShortName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return roomShortName+": "+status;
	}
}
