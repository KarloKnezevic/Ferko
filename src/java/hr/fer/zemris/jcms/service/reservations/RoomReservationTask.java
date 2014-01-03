package hr.fer.zemris.jcms.service.reservations;

/**
 * Pomocni razred koji sluzi za prenosenje statusa
 * rezervacija / odrezervacija.
 * 
 * @author marcupic
 *
 */
public class RoomReservationTask {

	private String roomShortName;
	private boolean success;
	private String message;
	
	public RoomReservationTask(String roomShortName) {
		super();
		this.roomShortName = roomShortName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

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
		RoomReservationTask other = (RoomReservationTask) obj;
		if (roomShortName == null) {
			if (other.roomShortName != null)
				return false;
		} else if (!roomShortName.equals(other.roomShortName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return roomShortName + ", success=" + success + (success ? "" : message);
	}
}
