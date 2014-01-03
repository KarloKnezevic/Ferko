package hr.fer.zemris.jcms.web.actions2.course.assessments.schedule;

import hr.fer.zemris.jcms.service2.course.assessments.schedule.AssessmentRoomService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentRoomScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.schedule.AssessmentRoomScheduleBuilder;

@WebClass(dataClass=AssessmentRoomScheduleData.class,defaultNavigBuilder=AssessmentRoomScheduleBuilder.class)
public class AssessmentRoomSchedule extends Ext2ActionSupport<AssessmentRoomScheduleData> {

	private static final long serialVersionUID = 2L;
	
	public static final String CONFIRM_AUTOCHOOSE = "confirmAutoChoose";
	public static final String CONFIRM_UPDATE = "confirmUpdate";

	@WebMethodInfo
	public String execute() throws Exception {
		return editRooms(); 
	}
	
	@WebMethodInfo
	public String editRooms() throws Exception {
		AssessmentRoomService.getRoomsForAssessment(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_CONFIRM,struts2Result=CONFIRM_UPDATE,registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=CONFIRM_UPDATE,navigBuilder=AssessmentRoomScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.confirmation"})})
	public String updateRooms() throws Exception {
		AssessmentRoomService.updateRoomsForAssessment(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo
	public String addRoomToList() throws Exception {
		AssessmentRoomService.addRoomToList(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_CONFIRM,struts2Result=CONFIRM_AUTOCHOOSE,registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=CONFIRM_AUTOCHOOSE,navigBuilder=AssessmentRoomScheduleBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.assessments.schedule.confirmation"})})
	public String autoChooseRooms() throws Exception {
		AssessmentRoomService.autoChooseRooms(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo
	public String getAvailableStatus() throws Exception {
		AssessmentRoomService.getAvailableStatus(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo
	public String syncReservations() throws Exception {
		AssessmentRoomService.syncReservations(getEntityManager(), data);
		return null;
	}

	public String getAssessmentID() {
		return data.getAssessmentID();
	}

	public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}

}
