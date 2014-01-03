package hr.fer.zemris.util.scheduling.algorithms.CLONALG;

import java.io.Serializable;

import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

public class Term implements ITerm,Serializable,Cloneable {
	

	private static final long serialVersionUID = -5493455178799229020L;
	private String id;
	private String name;
	private RoomData room;
	private DateStamp dateStamp;
	private TimeSpan timeSpan;
	private int duration;
	
	public Term(String id, String name) {
		this.id=id;
		this.name=name;
	}
	
	protected Object clone() {
		Term ret = new Term(id,name);
		ret.setRoom(room);
		ret.setDateStamp(dateStamp);
		ret.setTimeSpan(timeSpan);
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

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}
	
	
}