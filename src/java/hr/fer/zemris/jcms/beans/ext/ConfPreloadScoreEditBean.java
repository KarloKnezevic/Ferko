package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.beans.KeyValueBean;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.util.CreateIfNull;

public class ConfPreloadScoreEditBean {

	private Character letter;

	@CreateIfNull(value=true)
	private List<ConfPreloadScoreBean> items;
	
	private String assessmentID;
	private String courseInstanceID;
	
	private List<KeyValueBean> rooms = new ArrayList<KeyValueBean>();
	private String selectedRoomID;

	public String getSelectedRoomID() {
		return selectedRoomID;
	}
	public void setSelectedRoomID(String selectedRoomID) {
		this.selectedRoomID = selectedRoomID;
	}
	public List<KeyValueBean> getRooms() {
		return rooms;
	}
	public void setRooms(List<KeyValueBean> rooms) {
		this.rooms = rooms;
	}

	public ConfPreloadScoreEditBean() {
	}
	
	public Character getLetter() {
		return letter;
	}
	public void setLetter(Character letter) {
		this.letter = letter;
	}
	public List<ConfPreloadScoreBean> getItems() {
		return items;
	}
	public void setItems(List<ConfPreloadScoreBean> items) {
		this.items = items;
	}
	public String getAssessmentID() {
		return assessmentID;
	}
	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
