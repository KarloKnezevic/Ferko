package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

import java.util.Date;
import java.util.List;

public interface IScheduleTerm {

	public String getId();
	public int getSerialNumber();
	public List<String> getStudents();
	public String getTermName();
	public void overrideTermName(String newTermName);
	public RoomData getRoom();
	public DateStamp getDate();
	public TimeSpan getTermSpan();
	public String getOverridenEventName();
	public void setOverridenEventName(String eventName);
	public String getStartDateTime();
	public String getEndDateTime();
	public Date getStart();
}
