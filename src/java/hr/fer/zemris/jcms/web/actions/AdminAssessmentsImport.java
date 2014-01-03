package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.AssessmentScheduleBean;
import hr.fer.zemris.jcms.parsers.AssessmentScheduleParser;
import hr.fer.zemris.jcms.service.AssessmentEventService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentsScheduleEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.util.List;

import com.opensymphony.xwork2.Preparable;

public class AdminAssessmentsImport extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 2L;
	private String assessmentTag;
	private String currentYearSemesterID;
	private String assessmentsImport;
	
	private AssessmentsScheduleEditData data = null;
	
	@Override
	public void prepare() throws Exception {
		data = new AssessmentsScheduleEditData(MessageLoggerFactory.createMessageLogger(this));
		AssessmentEventService.getAssessmentEventsData(data);
	}
	
	@Override
	public String execute() throws Exception {
		return show();
	}
	
	public String show() throws Exception {
		
		if (!hasCurrentUser())
			return NO_PERMISSION;
		
		//provjeravamo smije li uopce korisnik vidjeti ovo
		AssessmentEventService.addAssessmentEvents(data, null, null, null,getCurrentUser().getUserID(), true);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		return INPUT;
	}
	
	public String update() throws Exception {
		
		if (!hasCurrentUser())
			return NO_PERMISSION;
		
		List<AssessmentScheduleBean> beanList = null;
		if (assessmentsImport != null) {
			try {
				beanList = AssessmentScheduleParser.parseTabbedFormat(new StringReader(assessmentsImport));
			} catch(ParseException ex) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
				return INPUT;
			}
		}
		AssessmentEventService.addAssessmentEvents(data, beanList, currentYearSemesterID, assessmentTag, getCurrentUser().getUserID(),false);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		data.getMessageLogger().registerAsDelayed();
		return SUCCESS;
	}
	
	

	public String getAssessmentTag() {
		return assessmentTag;
	}

	public void setAssessmentTag(String assessmentTag) {
		this.assessmentTag = assessmentTag;
	}

	public String getCurrentYearSemesterID() {
		return currentYearSemesterID;
	}

	public void setCurrentYearSemesterID(String currentYearSemesterID) {
		this.currentYearSemesterID = currentYearSemesterID;
	}
	
	public String getAssessmentsImport() {
		return assessmentsImport;
	}
	
	public void setAssessmentsImport(String assessmentsImport) {
		this.assessmentsImport = assessmentsImport;
	}

	public AssessmentsScheduleEditData getData() {
		return data;
	}

	public void setData(AssessmentsScheduleEditData data) {
		this.data = data;
	}
}
