package hr.fer.zemris.util.scheduling.algorithms.PSO;

import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.time.TimeStamp;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TermRecord {

	String eventId;
	String termId;
	String termName;
	List<String> students = new ArrayList<String>();
	String room;
	int roomCapacity;
	String date;
	TimeStamp fromTime;
	TimeStamp toTime;
	List<String> preconditions;
	List<TermRecord> rawTerms = null;
	
	public TermRecord(String eventId, String room, int roomCapacity, String date, TimeStamp fromTime, 
					TimeStamp toTime, List<String> preconditions)
	{
		this.eventId = eventId;
		this.room = room;
		this.roomCapacity = roomCapacity;
		this.date = date;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.preconditions = preconditions;
	}
	
	public TermRecord(ITerm t, String eventId)
	{
		this.eventId = eventId;
		this.room = t.getDefinition().getLocationParameters().get(0).getId();
		this.students.addAll(t.getDefinition().getIndividuals());
		this.roomCapacity = t.getDefinition().getLocationParameters().get(0).getActualCapacity();
		this.date = t.getDefinition().getTimeParameters().get(0).getFromDate().toString();
		this.fromTime = t.getDefinition().getTimeParameters().get(0).getFromTime();
		this.toTime = t.getDefinition().getTimeParameters().get(0).getToTime();
		this.preconditions = new ArrayList<String>();
		
	}
	
	public TermRecord(TermRecord t)
	{
		this.eventId = new String(t.eventId);
		this.students = new ArrayList<String>(t.students);
		this.room = new String(t.room);
		this.roomCapacity = t.roomCapacity;
		this.date = new String(t.date);
		this.fromTime = new TimeStamp(t.fromTime.getAbsoluteTime());
		this.toTime = new TimeStamp(t.toTime.getAbsoluteTime());
		this.preconditions = t.preconditions;
		if(t.rawTerms != null) 
		{
			this.rawTerms = new ArrayList<TermRecord>();
			for(TermRecord term : t.rawTerms)
				this.rawTerms.add(new TermRecord(term));
		}
	}
	
	public TermRecord(String event, String t)
	{
		String[] loc = t.split("\\#");
		String[] time = loc[1].split("\\$");
		this.room = new String(loc[0]);
		this.date = new String(time[0]);
		this.fromTime = new TimeStamp(time[1]);
		this.toTime = new TimeStamp(time[2]);
		this.eventId = new String(event);
	}

	public void setFromTime(TimeStamp fromTime)
	{
		this.fromTime = fromTime;
	}

	public void setToTime(TimeStamp toTime)
	{
		this.toTime = toTime;
	}

	public void setPreconditions(Set<IPrecondition> prec)
	{
		for(IPrecondition precondition : prec)
			this.preconditions.add(precondition.getEvent().getId());
	}
	
	public void setPreconditions(List<String> preconditions)
	{
		this.preconditions = preconditions;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String toString()
	{	
		return room + "#" + date + "$" + fromTime.toString() + "$" + toTime.toString();
	}
	
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		if(this.getClass().equals(obj.getClass()))
		{
			TermRecord t = (TermRecord) obj;
			if(this.toString().equals(t.toString())) return true;
		}
		else
		{
			String t = (String) obj;
			if(this.toString().equals(t)) return true;
		}
		return false;
	}
	
	public void setEventId(String eventId)
	{
		this.eventId = eventId;
	}
	
	public boolean hasSameStudentsAs(ArrayList<String> studentsList)
	{
		Set<String> studentsSet = new HashSet<String>(this.students);
		studentsSet.removeAll(new HashSet<String>(studentsList));
		if(studentsSet.isEmpty())
			return true;
		else 
			return false;
	}
	
	public int getDuration()
	{
		return this.toTime.getAbsoluteTime() - this.fromTime.getAbsoluteTime();
	}

}
