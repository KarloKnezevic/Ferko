package hr.fer.zemris.jcms.service.reservations;

/**
 * Vremenski period i status koji u tom periodu dvorana ima
 * 
 */
public class RoomReservationPeriod {
	
	private String roomShortName;
	private RoomReservationStatus status;
	private String date;
	private int timeFromOffset;
	private int timeToOffset;
	
	private String reason;
	
	public RoomReservationPeriod(String roomShortName, RoomReservationStatus status, String date, String timeStart, String timeEnd) {
		super();
		this.roomShortName = roomShortName;
		this.status = status;
		this.date=date;
		this.timeFromOffset = calcOffset(timeStart);
		this.timeToOffset = calcOffset(timeEnd);
	}
	
	/**
	 * 
	 * @param roomShortName
	 * @param status
	 * @param dateTimeFrom  Npr. 2009-10-10 10:10
	 * @param dateTimeTo    Npr. 2009-10-10 10:10
	 * @throws ReservationException
	 */
	public RoomReservationPeriod(String roomShortName, RoomReservationStatus status, String dateTimeFrom, String dateTimeTo) {
		String[] from = dateTimeFrom.split(" ");
		String[] to = dateTimeTo.split(" ");
		this.roomShortName = roomShortName;
		this.status = status;
		this.date=from[0];
		this.timeFromOffset = calcOffset(from[1]);
		this.timeToOffset = calcOffset(to[1]);
	}

	public RoomReservationStatus getStatus() {
		return status;
	}
	
	public void setStatus(RoomReservationStatus status) {
		this.status = status;
	}
	
	public String getRoomShortName() {
		return roomShortName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getTimeFromOffset() {
		return timeFromOffset;
	}

	public void setTimeFromOffset(int timeFromOffset) {
		this.timeFromOffset = timeFromOffset;
	}

	public int getTimeToOffset() {
		return timeToOffset;
	}

	public void setTimeToOffset(int timeToOffset) {
		this.timeToOffset = timeToOffset;
	}

	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDateTimeFrom(){
		return getDate() + " " + intToTime(timeFromOffset);
	}
	
	public String getDateTimeTo(){
		return getDate() + " " + intToTime(timeToOffset);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomShortName == null) ? 0 : roomShortName.hashCode());
		result = prime * result + timeFromOffset;
		result = prime * result + timeToOffset;
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
		RoomReservationPeriod other = (RoomReservationPeriod) obj;
		if (roomShortName == null) {
			if (other.roomShortName != null)
				return false;
		} else if (!roomShortName.equals(other.roomShortName))
			return false;
		if (timeFromOffset != other.timeFromOffset)
			return false;
		if (timeToOffset != other.timeToOffset)
			return false;
		
		return true;
	}
	
	public String getTimeSpanString(){
		return RoomReservationPeriod.intToTime(timeFromOffset)+"-"+RoomReservationPeriod.intToTime(timeToOffset);
	}
		
	@Override
	public String toString() {
		return roomShortName+": "+status+", " + intToTime(timeFromOffset) + "-" + intToTime(timeToOffset);
	}

	public String getFullTimeSpanString(){
		return roomShortName+": "+status+", " + date + " " + RoomReservationPeriod.intToTime(timeFromOffset)+"-"+RoomReservationPeriod.intToTime(timeToOffset);
	}

	private static String intToTime(int time) {
		int hours = time / 60;
		int minutes = time - hours*60;
		StringBuilder sb = new StringBuilder();
		if(hours<10) sb.append('0');
		sb.append(hours);
		sb.append(':');
		if(minutes<10) sb.append('0');
		sb.append(minutes);
		return sb.toString();
	}
	
	private static int calcOffset(String time) {
		return Integer.parseInt(time.substring(0,2))*60+Integer.parseInt(time.substring(3,5));
	}
}
