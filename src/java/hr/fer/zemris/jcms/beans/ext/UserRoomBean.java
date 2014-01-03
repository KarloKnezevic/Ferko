package hr.fer.zemris.jcms.beans.ext;

public class UserRoomBean {
	private String jmbag;
	private String shortRoomName;
	private int position = -1;
	
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getJmbag() {
		return jmbag;
	}
	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}
	public String getShortRoomName() {
		return shortRoomName;
	}
	public void setShortRoomName(String shortRoomName) {
		this.shortRoomName = shortRoomName;
	}
}
