package hr.fer.zemris.jcms.beans;

public class RoomBean {

	private String id;
	private String name;
	private String shortName;
	private String locator;
	private int lecturePlaces;
	private int exercisePlaces;
	private int assessmentPlaces;
	private int assessmentAssistants;
	private boolean publicRoom = true;
	private String venueShortName;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getLocator() {
		return locator;
	}
	public void setLocator(String locator) {
		this.locator = locator;
	}
	public int getLecturePlaces() {
		return lecturePlaces;
	}
	public void setLecturePlaces(int lecturePlaces) {
		this.lecturePlaces = lecturePlaces;
	}
	public int getExercisePlaces() {
		return exercisePlaces;
	}
	public void setExercisePlaces(int exercisePlaces) {
		this.exercisePlaces = exercisePlaces;
	}
	public int getAssessmentPlaces() {
		return assessmentPlaces;
	}
	public void setAssessmentPlaces(int assessmentPlaces) {
		this.assessmentPlaces = assessmentPlaces;
	}
	public int getAssessmentAssistants() {
		return assessmentAssistants;
	}
	public void setAssessmentAssistants(int assessmentAssistants) {
		this.assessmentAssistants = assessmentAssistants;
	}
	public boolean getPublicRoom() {
		return publicRoom;
	}
	public void setPublicRoom(boolean publicRoom) {
		this.publicRoom = publicRoom;
	}
	public String getVenueShortName() {
		return venueShortName;
	}
	public void setVenueShortName(String venueShortName) {
		this.venueShortName = venueShortName;
	}
}
