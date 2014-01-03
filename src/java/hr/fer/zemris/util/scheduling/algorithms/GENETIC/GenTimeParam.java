package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeStamp;

public class GenTimeParam implements ITimeParameter {
	private DateStamp fromDate;
	private DateStamp toDate;
	private TimeStamp fromTime;
	private TimeStamp toTime;

	public GenTimeParam(DateStamp fromDate, TimeStamp fromTime,
			DateStamp toDate, TimeStamp toTime) {
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	public GenTimeParam(ITimeParameter param) {
		this.fromDate = param.getFromDate();
		this.fromTime = param.getFromTime();
		this.toDate = param.getToDate();
		this.toTime = param.getToTime();
	}

	public void setFromDate(DateStamp fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(DateStamp toDate) {
		this.toDate = toDate;
	}

	public void setFromTime(TimeStamp fromTime) {
		this.fromTime = fromTime;
	}

	public void setToTime(TimeStamp toTime) {
		this.toTime = toTime;
	}

	@Override
	public DateStamp getFromDate() {
		return fromDate;
	}

	@Override
	public TimeStamp getFromTime() {
		return fromTime;
	}

	@Override
	public DateStamp getToDate() {
		return toDate;
	}

	@Override
	public TimeStamp getToTime() {
		return toTime;
	}

	@Override
	public String toString() {
		return (this.fromDate + " " + this.fromTime + "#" + this.toDate + " " + this.toTime);
	}
}
