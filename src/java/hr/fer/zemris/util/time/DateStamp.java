package hr.fer.zemris.util.time;

import java.io.Serializable;

public class DateStamp implements Comparable<DateStamp>, Serializable {

	private static final long serialVersionUID = -1577463628081745659L;
	private int number;
	private int year;
	private int month;
	private int day;
	private String stamp;
	
	public DateStamp(int year, int month, int day) {
		number = year * 400 + month*32 + day;
		StringBuilder sb = new StringBuilder();
		sb.append(year).append('-');
		if(month<10) sb.append('0');
		sb.append(month).append('-');
		if(day<10) sb.append('0');
		sb.append(day);
		stamp = sb.toString();
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public DateStamp(String date) {
		if(date.length()!=10) throw new DateStampParseException("Wrong length of date.");
		if(date.charAt(4)!='-' || date.charAt(7)!='-') throw new DateStampParseException("Wrong format of date.");
		try {
			this.year = Integer.parseInt(date.substring(0,4));
			this.month = Integer.parseInt(date.substring(5,7));
			this.day = Integer.parseInt(date.substring(8,10));
			this.number = year * 400 + month*32 + day;
			this.stamp = date;
		} catch(NumberFormatException ex) {
			throw new DateStampParseException("Wrong format of date.");
		}
	}

	public String getStamp() {
		return stamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
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
		DateStamp other = (DateStamp) obj;
		if (number != other.number)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		sb.append(stamp);
		return sb;
	}

	@Override
	public int compareTo(DateStamp o) {
		return this.number - o.number;
	}
	
	public static int dateDiff(DateStamp d1, DateStamp d2) {
		return d1.number-d2.number-1;
	}

}
