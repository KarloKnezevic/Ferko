package hr.fer.zemris.jcms.beans;

public class AssessmentRoomBean {
	private String id;
	private String name;
	private String capacity;
	private String requiredAssistants;
	private String roomTag;
	private String taken;
	private String roomStatus;
	
	private boolean available;

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

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getRequiredAssistants() {
		return requiredAssistants;
	}

	public void setRequiredAssistants(String requiredAssistants) {
		this.requiredAssistants = requiredAssistants;
	}

	public String getRoomTag() {
		return roomTag;
	}

	public void setRoomTag(String roomTag) {
		this.roomTag = roomTag;
	}

	public String getTaken() {
		return taken;
	}

	public void setTaken(String taken) {
		this.taken = taken;
	}

	public boolean getAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	public String getRoomStatus() {
		return roomStatus;
	}
	
	public void setRoomStatus(String roomStatus) {
		this.roomStatus = roomStatus;
	}
}
