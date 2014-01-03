package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.AssessmentAssistantBean;
import hr.fer.zemris.jcms.beans.ext.AssistantRoomBean;
import hr.fer.zemris.jcms.parsers.AssistantsJmbagParser;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.service.AssessmentAssistantService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentAssistantScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class AssessmentAssistantSchedule extends ExtendedActionSupport implements Preparable {
	
	private static final long serialVersionUID = 2L;
	
	private AssessmentAssistantScheduleData data;
	private List<AssessmentAssistantBean> assistantBeanList;
	private List<AssistantRoomBean> assistantRoomBeanList;
	private String assessmentID;
	private String importData;
	
	private static final String ASSISTANT_INPUT = "assistantInput";
	private static final String ASSISTANT_SUCCESS = "assistantSuccess";
	private static final String SCHEDULE_INPUT = "scheduleInput";
	private static final String SCHEDULE_SUCCESS = "scheduleSuccess";
	private static final String IMPORT_INPUT = "importInput";
	
	@Override
	public void prepare() throws Exception {
		data = new AssessmentAssistantScheduleData(MessageLoggerFactory.createMessageLogger(this, true));
		assistantBeanList = new ArrayList<AssessmentAssistantBean>();
		assistantRoomBeanList = new ArrayList<AssistantRoomBean>();
	}
	@Override
	public String execute() throws Exception {
		return editAssistants();
	}
	
	public String showImportAssistants() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentAssistantService.importAssistants(
				data, assessmentID, null,
				JCMSSecurityConstants.ROLE_ASISTENT, "edit", getCurrentUser().getUserID()
			);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
			return SHOW_FATAL_MESSAGE;
		
		return IMPORT_INPUT;
	}
	
	public String importAssistants() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		List<String> list = null;
		String type = null;
		if (importData != null) {
			try {
				list = AssistantsJmbagParser.parse(new StringReader(importData));
			}
			catch (Exception ex) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
				type = "edit";
			}
		} else {
			type = "edit";
		}
		
		AssessmentAssistantService.importAssistants(
				data, assessmentID, list, 
				JCMSSecurityConstants.ROLE_ASISTENT, type, getCurrentUser().getUserID()
		);
		
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
			return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return ASSISTANT_SUCCESS;
		}
		
		return IMPORT_INPUT;
	}
	
	public String editAssistants() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentAssistantService.assistantsEdit(
				data, assessmentID, assistantBeanList, 
				JCMSSecurityConstants.ROLE_ASISTENT, getCurrentUser().getUserID()
			);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return ASSISTANT_INPUT;
	}
	
	public String updateAssistants() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentAssistantService.assistantsUpdate(
				data, assessmentID, assistantBeanList, 
				JCMSSecurityConstants.ROLE_ASISTENT, getCurrentUser().getUserID()
			);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return ASSISTANT_SUCCESS;
	}
	
	public String editAssistantsSchedule() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		AssessmentAssistantService.editAssistantSchedule(data, assistantRoomBeanList, assessmentID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return SCHEDULE_INPUT;
	}
	
	public String updateAssistantsSchedule() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		AssessmentAssistantService.updateAssistantSchedule(data, assistantRoomBeanList, assessmentID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return SCHEDULE_SUCCESS;
	}
	
	public AssessmentAssistantScheduleData getData() {
		return data;
	}
	public void setData(AssessmentAssistantScheduleData data) {
		this.data = data;
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