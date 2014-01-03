package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

public class LabScheduleBean {
	public static class CategoryStudents {
		private String category;
		private List<String> jmbags;
		public CategoryStudents(String category, List<String> jmbags) {
			super();
			this.category = category;
			this.jmbags = jmbags;
		}
		public String getCategory() {
			return category;
		}
		public List<String> getJmbags() {
			return jmbags;
		}
	}

	private String kind;     // što je ovo točno? LAB, SEM, ZAD, ... U tom kontekstu oprezno s interpretacijom labNo
	private String isvuCode; // isvu sifra kolegija
	private String date;     // datum, u formatu yyyy-MM-dd
	private String start;    // kada započinje; u formatu HH:mm
	private int duration;    // trajanje u minutama
	private String room;     // dvorana
	private String venue;     // venue
	private List<CategoryStudents> students = new ArrayList<CategoryStudents>(); // kategorije koje tada imaju labos
	private int labNo;

	public LabScheduleBean() {
		students = new ArrayList<CategoryStudents>();
	}

	public LabScheduleBean(String isvuCode, String date, String start, int duration, String room, String venue, int labNo, String kind) {
		super();
		this.date = date;
		this.duration = duration;
		this.isvuCode = isvuCode;
		this.start = start;
		this.room = room;
		this.venue = venue;
		this.labNo = labNo;
		this.kind = kind;
	}

	public int getLabNo() {
		return labNo;
	}
	
	public void setLabNo(int labNo) {
		this.labNo = labNo;
	}
	
	public String getIsvuCode() {
		return isvuCode;
	}

	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public String getKind() {
		return kind;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
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

	public List<CategoryStudents> getStudents() {
		return students;
	}
	
	public void setStudents(List<CategoryStudents> students) {
		this.students = students;
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}
}
