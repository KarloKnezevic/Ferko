package hr.fer.zemris.jcms.service.reservations;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Razred koji predstavlja jednu rezervaciju u sustavu rezervacija.
 * 
 * @author marcupic
 *
 */
public class RoomReservationEntry {
	
	private static final long serialVersionUID = 1L;
	
	private long fromtime;
	private long totime;
	private String reason;
	private String jmbag;
	private String room;
	
	public RoomReservationEntry(String room, String jmbag, long fromtime,
			long totime, String reason) {
		super();
		this.room = room;
		this.jmbag = jmbag;
		this.fromtime = fromtime;
		this.totime = totime;
		this.reason = reason;
	}
	
	public long getFromtime() {
		return fromtime;
	}
	
	public long getTotime() {
		return totime;
	}
	
	public String getReason() {
		return reason;
	}
	
	public String getJmbag() {
		return jmbag;
	}
	
	public String getRoom() {
		return room;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fromtime ^ (fromtime >>> 32));
		result = prime * result + ((jmbag == null) ? 0 : jmbag.hashCode());
		result = prime * result
				+ ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		result = prime * result + (int) (totime ^ (totime >>> 32));
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
		RoomReservationEntry other = (RoomReservationEntry) obj;
		if (fromtime != other.fromtime)
			return false;
		if (jmbag == null) {
			if (other.jmbag != null)
				return false;
		} else if (!jmbag.equals(other.jmbag))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		if (totime != other.totime)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return room+" "+sdf.format(new Date(fromtime))+" - "+sdf.format(new Date(totime)) + ", "+jmbag+", "+reason;
	}
}
