package hr.fer.zemris.jcms.web.actions2.course.assessments.schedule;

import hr.fer.zemris.jcms.service2.course.assessments.schedule.AssessmentScheduleService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.schedule.AssessmentScheduleBuilder;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

@WebClass(dataClass=AssessmentScheduleData.class, defaultNavigBuilder=AssessmentScheduleBuilder.class)
public class AssessmentSchedule extends Ext2ActionSupport<AssessmentScheduleData> {
	
private static final long serialVersionUID = 1L;
	
	private static final String RESULT_INFO = "info";
	private static final String CONFIRM_SYNCHRONIZE = "confirmSynchronize";
	private static final String CONFIRM_MAKESCHEDULE = "confirmMakeSchedule";
	private static final String CONFIRM_IMPORTSCHEDULE = "confirmImportSchedule";
	
	@WebMethodInfo
	public String execute() throws Exception {
		return viewRoomList();
	}

	/**
	 * Metoda crta početni ekran za izradu rasporeda.
	 * 
	 * @return <code>null</code>
	 * @throws Exception
	 */
	@WebMethodInfo
	public String viewRoomList() throws Exception {
		AssessmentScheduleService.fetchScheduleMenu(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=RESULT_INFO,registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=RESULT_INFO,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.roomInfo"})})
	public String viewRoomInfo() throws Exception {
		AssessmentScheduleService.fetchRoomInfo(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
	public String downloadListings() throws Exception {
		AssessmentScheduleService.prepareListingsPDF(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
	public String downloadSchedule() throws Exception {
		AssessmentScheduleService.prepareSchedulePDF(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
	public String downloadMailMerge() throws Exception {
		AssessmentScheduleService.prepareMailMerge(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
	public String downloadAssessmentInfo() throws Exception {
		AssessmentScheduleService.prepareAssessmentInfo(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
	public String downloadSheets() throws Exception {
		AssessmentScheduleService.prepareAnswerSheetsPDF(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=CONFIRM_SYNCHRONIZE,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=CONFIRM_SYNCHRONIZE,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.confirmation"}),
			@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class)})
	public String synchronizeStudents() throws Exception {
		AssessmentScheduleService.retrieveAssessmentStudents(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=CONFIRM_MAKESCHEDULE,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=CONFIRM_MAKESCHEDULE,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.confirmation"}),
			@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class)})
	public String makeStudentSchedule() throws Exception {
		AssessmentScheduleService.makeStudentSchedule(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class)})
	public String broadcastEvents() throws Exception {
		AssessmentScheduleService.publishEvents(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=INPUT,registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.import"})})
	public String importScheduleEdit() throws Exception {
		AssessmentScheduleService.importSchedulePrepare(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=CONFIRM_IMPORTSCHEDULE,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=CONFIRM_IMPORTSCHEDULE,navigBuilder=AssessmentScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.confirmation"}),
			@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class)})
	public String importScheduleUpdate() throws Exception {
		AssessmentScheduleService.importSchedule(getEntityManager(), data);
		return null;
	}
	
	public String getAssessmentID() {
		return data.getAssessmentID();
	}

	public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}

	public String getAssessmentRoomID() {
		return data.getAssessmentRoomID();
	}

	public void setAssessmentRoomID(String assessmentRoomID) {
		data.setAssessmentRoomID(assessmentRoomID);
	}

	public DeleteOnCloseFileInputStream getStream() {
		return data.getStream();
	}
	
	public String getType() {
		return data.getType();
	}
	public void setType(String type) {
		data.setType(type);
	}

	public String getProportional() {
		return data.getProportional();
	}
	public void setProportional(String proportional) {
		data.setProportional(proportional);
	}

	public String getCp() {
		return data.getCp();
	}
	public void setCp(String cp) {
		data.setCp(cp);
	}

}
