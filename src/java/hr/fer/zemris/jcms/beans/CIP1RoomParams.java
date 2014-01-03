package hr.fer.zemris.jcms.beans;

public class CIP1RoomParams {
	
	String roomId;
	String roomName;
	int students;
	int assistants;
	int defaultStudents;
	int defaultAsistants;
	
	public CIP1RoomParams() {
	}

	public CIP1RoomParams(String roomId, String roomName, int students,
			int assistants, int defaultStudents, int defaultAsistants) {
		super();
		this.roomId = roomId;
		this.roomName = roomName;
		this.students = students;
		this.assistants = assistants;
		this.defaultStudents = defaultStudents;
		this.defaultAsistants = defaultAsistants;
	}

	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public int getStudents() {
		return students;
	}
	public void setStudents(int students) {
		this.students = students;
	}
	public int getAssistants() {
		return assistants;
	}
	public void setAssistants(int assistants) {
		this.assistants = assistants;
	}

	public int getDefaultStudents() {
		return defaultStudents;
	}

	public void setDefaultStudents(int defaultStudents) {
		this.defaultStudents = defaultStudents;
	}

	public int getDefaultAsistants() {
		return defaultAsistants;
	}

	public void setDefaultAsistants(int defaultAsistants) {
		this.defaultAsistants = defaultAsistants;
	}
}
