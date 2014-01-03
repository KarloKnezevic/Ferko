package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.beans.KeyValueBean;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.util.CreateIfNull;

public class ConfProblemsScoreEditBean {

	private Character letter;

	@CreateIfNull(value=true)
	private List<ConfProblemsScoreBean> items;
	
	private String assessmentID;
	private String courseInstanceID;
	private int numberOfProblems;

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
	
	public ConfProblemsScoreEditBean() {
	}
	
	public Character getLetter() {
		return letter;
	}
	public void setLetter(Character letter) {
		this.letter = letter;
	}
	public List<ConfProblemsScoreBean> getItems() {
		return items;
	}
	public void setItems(List<ConfProblemsScoreBean> items) {
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

	public int getNumberOfProblems() {
		return this.numberOfProblems;
	}

	public void setNumberOfProblems(int numberOfProblems) {
		this.numberOfProblems = numberOfProblems;
	}
}
