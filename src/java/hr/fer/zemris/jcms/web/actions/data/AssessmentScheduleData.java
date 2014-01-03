package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.AssessmentRoomArrangedBean;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AssessmentScheduleData extends BaseAssessment {

	List<User> userList = Collections.emptyList();
	List<AssessmentRoomArrangedBean> roomList = Collections.emptyList();
	List<User> assistantList = Collections.emptyList();
	String roomName;
	String assessmentID;
	String assessmentRoomID;
	DeleteOnCloseFileInputStream stream;
	String scheduleAssessmentID;
	String cp = "0";
	
	private String scheduleImport;
	private String doit;
	private String type;
	private String proportional;
	
	private Map<String, String> importTypes;

	int studentsFetched = 0;
	int roomsFetched = 0;
	int studentScheduleCreated = 0;
	int assistantsFetched = 0;
	int assistantScheduleCreated = 0;
	int schedulePublished = 0;
	
	public AssessmentScheduleData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public List<AssessmentRoomArrangedBean> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<AssessmentRoomArrangedBean> roomList) {
		this.roomList = roomList;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public List<User> getAssistantList() {
		return assistantList;
	}

	public void setAssistantList(List<User> assistantList) {
		this.assistantList = assistantList;
	}
	
	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public String getAssessmentRoomID() {
		return assessmentRoomID;
	}

	public void setAssessmentRoomID(String assessmentRoomID) {
		this.assessmentRoomID = assessmentRoomID;
	}

	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
	public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}

	public int getStudentsFetched() {
		return studentsFetched;
	}

	public void setStudentsFetched(int studentsFetched) {
		this.studentsFetched = studentsFetched;
	}

	public int getRoomsFetched() {
		return roomsFetched;
	}

	public void setRoomsFetched(int roomsFetched) {
		this.roomsFetched = roomsFetched;
	}

	public int getStudentScheduleCreated() {
		return studentScheduleCreated;
	}

	public void setStudentScheduleCreated(int studentScheduleCreated) {
		this.studentScheduleCreated = studentScheduleCreated;
	}

	public int getAssistantsFetched() {
		return assistantsFetched;
	}

	public void setAssistantsFetched(int assistantsFetched) {
		this.assistantsFetched = assistantsFetched;
	}

	public int getAssistantScheduleCreated() {
		return assistantScheduleCreated;
	}

	public void setAssistantScheduleCreated(int assistantScheduleCreated) {
		this.assistantScheduleCreated = assistantScheduleCreated;
	}

	public int getSchedulePublished() {
		return schedulePublished;
	}

	public void setSchedulePublished(int schedulePublished) {
		this.schedulePublished = schedulePublished;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getScheduleImport() {
		return scheduleImport;
	}

	public void setScheduleImport(String scheduleImport) {
		this.scheduleImport = scheduleImport;
	}

	public Map<String, String> getImportTypes() {
		if(importTypes==null) {
			importTypes = new LinkedHashMap<String, String>();
			importTypes.put("1", getMessageLogger().getText("forms.mailMerge"));
			importTypes.put("2", getMessageLogger().getText("forms.tabbedFormat"));
		}
		
		return importTypes;
	}

	public String getScheduleAssessmentID() {
		return scheduleAssessmentID;
	}
	public void setScheduleAssessmentID(String scheduleAssessmentID) {
		this.scheduleAssessmentID = scheduleAssessmentID;
	}
	
	public String getProportional() {
		return proportional;
	}
	public void setProportional(String proportional) {
		this.proportional = proportional;
	}
	
	public String getCp() {
		return cp;
	}
	public void setCp(String cp) {
		this.cp = cp;
	}
}
