package hr.fer.zemris.jcms.web.actions2.course.assessments.schedule;

import hr.fer.zemris.jcms.service2.course.assessments.schedule.AssessmentAssistantService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentAssistantScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.schedule.AssessmentScheduleBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.schedule.AssessmentsImportBuilder;

@WebClass(dataClass=AssessmentAssistantScheduleData.class)
public class AssessmentAssistantSchedule extends Ext2ActionSupport<AssessmentAssistantScheduleData> {
	
	private static final long serialVersionUID = 2L;

	private static final String ASSISTANT_INPUT = "assistantInput";
	private static final String ASSISTANT_SUCCESS = "assistantSuccess";
	private static final String SCHEDULE_INPUT = "scheduleInput";
	private static final String SCHEDULE_SUCCESS = "scheduleSuccess";
	private static final String IMPORT_INPUT = "importInput";

	@WebMethodInfo
	public String execute() throws Exception {
		return editAssistants();
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=ASSISTANT_INPUT,registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=ASSISTANT_INPUT,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.assistants"})})
	public String editAssistants() throws Exception {
		AssessmentAssistantService.assistantsEdit(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=ASSISTANT_SUCCESS,registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=ASSISTANT_SUCCESS,navigBuilder=DefaultNavigationBuilder.class)})
	public String updateAssistants() throws Exception {
		AssessmentAssistantService.assistantsUpdate(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=IMPORT_INPUT,registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=IMPORT_INPUT,navigBuilder=AssessmentsImportBuilder.class)})
	public String showImportAssistants() throws Exception {
		AssessmentAssistantService.importAssistantsPrepare(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=IMPORT_INPUT,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=ASSISTANT_SUCCESS,registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=IMPORT_INPUT,navigBuilder=AssessmentsImportBuilder.class),
			@Struts2ResultMapping(struts2Result=ASSISTANT_SUCCESS,navigBuilder=DefaultNavigationBuilder.class)
		})
	public String importAssistants() throws Exception {
		AssessmentAssistantService.importAssistants(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=SCHEDULE_INPUT,registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=SCHEDULE_INPUT,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.assistants"})})
	public String editAssistantsSchedule() throws Exception {
		AssessmentAssistantService.editAssistantSchedule(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SCHEDULE_SUCCESS,registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=SCHEDULE_SUCCESS,navigBuilder=DefaultNavigationBuilder.class)})
	public String updateAssistantsSchedule() throws Exception {
		AssessmentAssistantService.updateAssistantSchedule(getEntityManager(), data);
		return null;
	}
	
	public String getAssessmentID() {
		return data.getAssessmentID();
	}
	public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
}