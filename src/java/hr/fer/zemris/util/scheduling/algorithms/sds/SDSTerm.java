package hr.fer.zemris.util.scheduling.algorithms.sds;

import hr.fer.zemris.util.scheduling.support.IScheduleTerm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import java.util.*;

public class SDSTerm implements IScheduleTerm, ITerm {

	private DateStamp date;
	private Date start;
	private String id;
	private String termName;
	private RoomData room;
	private List<String> students = new ArrayList<String>();
	private boolean studentsChangable = true;
	private List<RoomData> termRooms = new ArrayList<RoomData>();
	private TimeSpan termSpan;
	ISchedulingData termData;
	ISchedulingData eventData;
	private int order;

	public SDSTerm(String id, String name) {
		this.termName = name;
		this.id = id;
	}

	public boolean isStudentsChangable() {
		return studentsChangable;
	}

	public ISchedulingData getTermData() {
		return termData;
	}

	public void setTermData(ISchedulingData termData) {
		this.termData = termData;
	}

	public ISchedulingData getEventData() {
		return eventData;
	}

	public void setEventData(ISchedulingData eventData) {
		this.eventData = eventData;
	}

	public void setStudentsChangable(boolean studentsChangable) {
		this.studentsChangable = studentsChangable;
	}

	public void setDate(DateStamp date) {
		this.date = date;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public List<RoomData> getTermRooms() {
		return termRooms;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public void setRoom(RoomData room) {
		this.room = room;
	}

	public void setStudents(List<String> students) {
		this.students = students;
	}

	public void setTermSpan(TimeSpan termSpan) {
		this.termSpan = termSpan;
	}

	@Override
	public DateStamp getDate() {
		return date;
	}

	@Override
	public String getEndDateTime() {
		return getDate().toString() + " " + getTermSpan().getEnd().toString();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getOverridenEventName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoomData getRoom() {
		return room;
	}

	@Override
	public int getSerialNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public String getStartDateTime() {
		return getDate().toString() + " " + getTermSpan().getStart().toString();
	}

	@Override
	public List<String> getStudents() {
		return students;
	}

	@Override
	public String getTermName() {
		return termName;
	}

	@Override
	public TimeSpan getTermSpan() {
		return termSpan;
	}

	@Override
	public void overrideTermName(String newTermName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOverridenEventName(String eventName) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDefinition getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return termName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SDSTerm))
			return false;
		SDSTerm pom = (SDSTerm) obj;
		if (pom.getId().equals(this.id))
			return true;
		return false;
	}

	public boolean equals2(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof SDSTerm))
			return false;
		SDSTerm pom = (SDSTerm) obj;
		if (!this.termRooms.equals(pom.termRooms))
			return false;
		if (termData != null && pom.getTermData() != null)
			if (!this.termData.equals(pom.getTermData()))
				return false;
		if (eventData != null && pom.getEventData() != null)
			if (!this.eventData.equals(pom.getEventData()))
				return false;
		return true;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	@Override
	public String toString() {
		return room.getId() + " " + date + " " + termSpan + " ";
	}
	
	@Override
	public int hashCode() {
		
		return id.hashCode();
	}

}
