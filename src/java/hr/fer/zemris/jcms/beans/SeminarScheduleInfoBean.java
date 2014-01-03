package hr.fer.zemris.jcms.beans;

public class SeminarScheduleInfoBean {
	private String jmbag;
	private String dateTime;
	private String roomText;
	
	public SeminarScheduleInfoBean() {
	}

	public String getJmbag() {
		return jmbag;
	}

	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getRoomText() {
		return roomText;
	}

	public void setRoomText(String roomText) {
		this.roomText = roomText;
	}
	
}
