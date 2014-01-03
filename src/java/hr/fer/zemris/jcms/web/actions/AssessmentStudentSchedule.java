package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.UserRoomBean;
import hr.fer.zemris.jcms.parsers.UserRoomParser;
import hr.fer.zemris.jcms.service.AssessmentStudentService;
import hr.fer.zemris.jcms.web.actions.data.BaseAssessment;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class AssessmentStudentSchedule extends ExtendedActionSupport  implements Preparable {
	
	private static final long serialVersionUID = 2L;
	private BaseAssessment data;
	private String assessmentID;
	private String scheduleImport;
	private String doit;
	private String type;
	
	private Map<String, String> importTypes;
	
	public static String CONFIRM_SYNCHRONIZE = "confirmSynchronize";
	public static String CONFIRM_MAKESCHEDULE = "confirmMakeSchedule";
	public static String CONFIRM_IMPORTSCHEDULE = "confirmImportSchedule";
	
	@Override
	public void prepare() throws Exception {
		data = new BaseAssessment(MessageLoggerFactory.createMessageLogger(this));
	}
	
	@Override
	public String execute() throws Exception {
		return SHOW_FATAL_MESSAGE;
	}
	
	public String synchronizeStudents() throws Exception {
		if (!hasCurrentUser()) {
			return NO_PERMISSION;
		}
		AssessmentStudentService.getStudents(data, assessmentID,doit,getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT))
			return CONFIRM_SYNCHRONIZE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String makeStudentSchedule() throws Exception {
		if (!hasCurrentUser()) {
			return NO_PERMISSION;
		}
		AssessmentStudentService.makeStudentSchedule(data, assessmentID, type,doit,getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT))
			return CONFIRM_MAKESCHEDULE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String broadcastEvents() throws Exception {
		if (!hasCurrentUser()) {
			return NO_PERMISSION;
		}
		AssessmentStudentService.broadcastEvents(data, assessmentID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS) 
				|| data.getResult().equals(AbstractActionData.RESULT_NONFATAL_ERROR)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String importScheduleEdit() throws Exception {
		if (!hasCurrentUser()) {
			return NO_PERMISSION;
		}
		AssessmentStudentService.importSchedule(data, null, assessmentID, "edit",getCurrentUser().getUserID(),"FER");
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
			return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String importScheduleUpdate() throws Exception {
		if (!hasCurrentUser()) {
			return NO_PERMISSION;
		}
		List<UserRoomBean> beanList = null;
		if (scheduleImport != null) {
			try { 
				if ("1".equals(type))
					beanList = UserRoomParser.parseMailMerge(new StringReader(scheduleImport));
				else if ("2".equals(type))
					beanList = UserRoomParser.parseTabbedFormat(new StringReader(scheduleImport));
				else
					return SHOW_FATAL_MESSAGE;
				
			} catch(ParseException ex) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
					AssessmentStudentService.importSchedule(data, null, assessmentID, "edit", getCurrentUser().getUserID(),"FER");
					if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
						return SHOW_FATAL_MESSAGE;
					return INPUT;
			}
			AssessmentStudentService.importSchedule(data, beanList, assessmentID, doit, getCurrentUser().getUserID(),"FER");
			if (data.getResult().equals(AbstractActionData.RESULT_INPUT))
				return CONFIRM_IMPORTSCHEDULE;
			if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
				data.getMessageLogger().registerAsDelayed();
				return SUCCESS;
			}
			if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
				return SHOW_FATAL_MESSAGE;
		}
		AssessmentStudentService.importSchedule(data, null, assessmentID, "edit",getCurrentUser().getUserID(),"FER");
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
			return SHOW_FATAL_MESSAGE;
		return INPUT;
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public BaseAssessment getData() {
		return data;
	}

	public void setData(BaseAssessment data) {
		this.data = data;
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
			importTypes.put("1", getText("forms.mailMerge"));
			importTypes.put("2", getText("forms.tabbedFormat"));
		}
		
		return importTypes;
	}

}
