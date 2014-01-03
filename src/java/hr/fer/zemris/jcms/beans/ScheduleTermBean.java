package hr.fer.zemris.jcms.beans;

import java.util.List;

public class ScheduleTermBean{
	private String id;
	private String name;
	private String roomName;
	private List<String> jmbags;
	private String termStart;
	private String termEnd;
	private int numberOfStudents;
	private String students;
	//Naziv eventa pridruzenog terminu
	private String eventName;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getNumberOfStudents() {
		return numberOfStudents;
	}
	public void setNumberOfStudents(int numberOfStudents) {
		this.numberOfStudents = numberOfStudents;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public List<String> getJmbags() {
		return jmbags;
	}
	public void setJmbags(List<String> jmbags) {
		this.jmbags = jmbags;
	}
	public String getTermStart() {
		return termStart;
	}
	public void setTermStart(String termStart) {
		this.termStart = termStart;
	}
	public String getTermEnd() {
		return termEnd;
	}
	public void setTermEnd(String termEnd) {
		this.termEnd = termEnd;
	}
	public String getStudents() {
		return students;
	}
	public void setStudents(String students) {
		this.students = students;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	
}
