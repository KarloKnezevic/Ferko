package hr.fer.zemris.util.scheduling.support;

import java.util.List;

public interface ISchedule {
	
	public String getName();
	public List<IScheduleEvent> getScheduleEvents();
	public IScheduleEvent getEventForId(String eventId);
	
	public String toXMLString();
}
