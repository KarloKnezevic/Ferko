package hr.fer.zemris.util.time;

import java.io.Serializable;

public class TimeSpan implements Comparable<TimeSpan>, Serializable {

	private static final long serialVersionUID = 4710128297866674967L;
	private TimeStamp start;
	private TimeStamp end;
	public TimeSpan(TimeStamp start, TimeStamp end) {
		super();
		this.start = start;
		this.end = end;
	}
	public TimeStamp getStart() {
		return start;
	}
	public TimeStamp getEnd() {
		return end;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
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
		TimeSpan other = (TimeSpan) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		return true;
	}

	@Override
	public int compareTo(TimeSpan o) {
		int r = this.start.compareTo(o.start);
		if(r!=0) return r;
		return this.end.compareTo(o.end);
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		start.toString(sb);
		sb.append(" - ");
		end.toString(sb);
		return sb;
	}
	
	public int getDuration() {
		return end.getAbsoluteTime()-start.getAbsoluteTime();
	}
}
