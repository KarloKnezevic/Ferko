package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

public class GroupScheduleBean {
	private String isvuCode; // isvu sifra kolegija
	private String date;     // datum, u formatu yyyy-MM-dd
	private String start;    // kada započinje; u formatu HH:mm
	private int duration;    // trajanje u minutama
	private String room;     // dvorana
	private String venue;     // venue
	private List<String> groups; // grupe koje tada imaju predavanje

	public GroupScheduleBean() {
		groups = new ArrayList<String>();
	}

	public GroupScheduleBean(String isvuCode, String date, int duration, String start, String room, String venue) {
		super();
		this.date = date;
		this.duration = duration;
		this.isvuCode = isvuCode;
		this.start = start;
		this.room = room;
		this.venue = venue;
	}

	public String getIsvuCode() {
		return isvuCode;
	}

	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getRoom() {
		return room;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public List<String> getGroups() {
		return groups;
	}
	
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}
}
