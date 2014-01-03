package hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic;

import java.io.Serializable;

import java.util.Map;

import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

public class TermBCO implements ITerm,Serializable,Cloneable {
	

	private static final long serialVersionUID = -5493455178799229020L;
	private String id;
	private String name;
	private RoomData room;
	private DateStamp dateStamp;
	private TimeSpan timeSpan;
	private RoomData[] availableRooms;
	private DateStamp[] availableDates;
	private Map<DateStamp, TimeSpan[]> availableTimeSpans;
	private int duration;
	
	public TermBCO(String id, String name) {
		this.id=id;
		this.name=name;
	}
	
	protected Object clone() {
		TermBCO ret = new TermBCO(id,name);
		ret.setRoom(room);
		ret.setDateStamp(dateStamp);
		ret.setTimeSpan(timeSpan);
		ret.setAvailableRooms(availableRooms);
		ret.setAvailableDates(availableDates);
		ret.setAvailableTimeSpans(availableTimeSpans);
		ret.setDuration(duration);
		return ret;
	}
	
	public void setRoom(RoomData room) {
		this.room=room;
	}
	
	public void setTimeSpan(TimeSpan timeSpan) {
		this.timeSpan=timeSpan;
	}
	
	public void setDateStamp(DateStamp dateStamp) {
		this.dateStamp=dateStamp;
	}
	
	public RoomData getRoom() {
		return room;
	}
	
	public DateStamp getDate() {
		return dateStamp;
	}
	
	public TimeSpan getTimeSpan() {
		return timeSpan;
	}
	@Override
	public IDefinition getDefinition() {
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setAvailableRooms(RoomData[] termRooms) {
		availableRooms=termRooms;
	}

	public void setAvailableDates(DateStamp[] availableDates2) {
		this.availableDates=availableDates2;
	}

	public void setAvailableTimeSpans(Map<DateStamp, TimeSpan[]> availableTimeSpansForDate) {
		this.availableTimeSpans=availableTimeSpansForDate;
	}

	public RoomData[] getAvailableRooms() {
		return availableRooms;
	}

	public DateStamp[] getAvailableDates() {
		return availableDates;
	}

	public Map<DateStamp, TimeSpan[]> getAvailableTimeSpans() {
		return availableTimeSpans;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}
	
	
}