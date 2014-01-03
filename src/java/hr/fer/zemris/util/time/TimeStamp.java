package hr.fer.zemris.util.time;

import java.io.Serializable;

public class TimeStamp implements Comparable<TimeStamp>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6940652979551713298L;
	private int hour;
	private int minute;
	private int absoluteTime;
	
	public TimeStamp(int hour, int minute) {
		super();
		this.hour = hour;
		this.minute = minute;
		this.absoluteTime = hour * 60 + minute;
	}

	public TimeStamp(String time){
		if(time.length()!=5) throw new TimeStampParseException("Wrong length of time.");
		if(time.charAt(2)!=':') throw new TimeStampParseException("Wrong format of time.");
		try {
			this.hour = Integer.parseInt(time.substring(0,2));
			this.minute = Integer.parseInt(time.substring(3,5));
			this.absoluteTime = hour * 60 + minute;
		} catch(NumberFormatException ex) {
			throw new TimeStampParseException("Wrong format of time.");
		}
	}
	
	public TimeStamp(int absoluteTime){
		super();
		this.hour = absoluteTime / 60;
		this.minute = absoluteTime % 60;
		this.absoluteTime=absoluteTime;
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}

	public int getAbsoluteTime() {
		return absoluteTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + absoluteTime;
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
		TimeStamp other = (TimeStamp) obj;
		if (absoluteTime != other.absoluteTime)
			return false;
		return true;
	}

	@Override
	public int compareTo(TimeStamp o) {
		return this.absoluteTime - o.absoluteTime;
	}
	
	public boolean after(TimeStamp timeStamp) {
		return this.absoluteTime > timeStamp.absoluteTime;
	}

	public boolean afterOrAt(TimeStamp timeStamp) {
		return this.absoluteTime >= timeStamp.absoluteTime;
	}

	public boolean before(TimeStamp timeStamp) {
		return this.absoluteTime < timeStamp.absoluteTime;
	}

	public boolean beforeOrAt(TimeStamp timeStamp) {
		return this.absoluteTime <= timeStamp.absoluteTime;
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		if(hour<10) sb.append('0');
		sb.append(hour).append(':');
		if(minute<10) sb.append('0');
		sb.append(minute);
		return sb;
	}
	
	/**
	 * Vraca novi timestamp koji se dobije tako da se trenutnom
	 * pridoda vremenski offset izrazen u satima i minutama. Broj
	 * minuta moze biti i veci od 59, i u tom ce se slucaju izracunati
	 * ispravni sati i minute. Medutim, novo vrijeme ne smije prijeci
	 * granicu od 24 sata; u tom ce se slucaju generirati iznimka!
	 * @param timeStampCache timestamp cache to use for timestamp construction
	 * @param hours number of hours to add
	 * @param minutes number of minutes to add
	 * @return new timestamp
	 * @throws TimeStampOverflowException if new timestamp crosses day boundary
	 */
	public TimeStamp add(TimeStampCache timeStampCache, int hours, int minutes) {
		int h = this.hour + hours;
		int m = this.minute + minutes;
		int hm = m / 60;
		h += hm;
		m = m % 60;
		if(h>24 || (h==24 && m>0)) throw new TimeStampOverflowException("TimeStamp with h="+h+" and m="+m+" is illegal.");
		return timeStampCache.get(h, m);
	}
}
