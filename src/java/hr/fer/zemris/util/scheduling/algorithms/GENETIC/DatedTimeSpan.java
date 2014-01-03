package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

public class DatedTimeSpan {
	private DateStamp date;
	private TimeSpan span;

	public DatedTimeSpan(DateStamp date, TimeSpan span) {
		this.date = date;
		this.span = span;
	}

	public DateStamp getDate() {
		return this.date;
	}

	public TimeSpan getSpan() {
		return this.span;
	}

	@Override
	public String toString() {
		return (this.date + " " + this.span);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		DatedTimeSpan other = (DatedTimeSpan) obj;

		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;

		if (span == null) {
			if (other.span != null)
				return false;
		} else if (!span.equals(other.span))
			return false;

		return true;
	}
}
