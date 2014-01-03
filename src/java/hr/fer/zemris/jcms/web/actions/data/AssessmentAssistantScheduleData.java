package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.AssessmentRoomBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentAssistantBean;
import hr.fer.zemris.jcms.beans.ext.AssistantRoomBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssessmentAssistantScheduleData extends BaseAssessment {
	
	private List<AssessmentAssistantBean> assistantBeanList;
	private List<AssistantRoomBean> assistantRoomBeanList;
	private String importData;
	private int assistantsRequired;
	private List<AssessmentRoomBean> roomList = Collections.emptyList();
	private String assessmentID;
	
	public AssessmentAssistantScheduleData(IMessageLogger messageLogger) {
		super(messageLogger);
		assistantBeanList = new ArrayList<AssessmentAssistantBean>();
		assistantRoomBeanList = new ArrayList<AssistantRoomBean>();
	}

	public int getAssistantsRequired() {
		return assistantsRequired;
	}

	public void setAssistantsRequired(int assistantsRequired) {
		this.assistantsRequired = assistantsRequired;
	}

	public List<AssessmentRoomBean> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<AssessmentRoomBean> roomList) {
		this.roomList = roomList;
	}
	
	public String getAssessmentID() {
		return assessmentID;
	}
	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
	
	public List<AssessmentAssistantBean> getAssistantBeanList() {
		return assistantBeanList;
	}
	public void setAssistantBeanList(List<AssessmentAssistantBean> assistantBeanList) {
		this.assistantBeanList = assistantBeanList;
	}
	
	public List<AssistantRoomBean> getAssistantRoomBeanList() {
		return assistantRoomBeanList;
	}
	public void setAssistantRoomBeanList(
			List<AssistantRoomBean> assistantRoomBeanList) {
		this.assistantRoomBeanList = assistantRoomBeanList;
	}
	public String getImportData() {
		return importData;
	}
	public void setImportData(String importData) {
		this.importData = importData;
	}

}
