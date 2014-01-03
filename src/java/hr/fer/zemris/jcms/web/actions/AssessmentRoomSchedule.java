package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.AssessmentRoomBean;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;
import hr.fer.zemris.jcms.service.AssessmentRoomService;
import hr.fer.zemris.jcms.web.actions.data.AssessmentRoomScheduleData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class AssessmentRoomSchedule extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;
	
	List<AssessmentRoomBean> roomList = null;
	private Map<String,String> tagCache;
	AssessmentRoomScheduleData data = null;
	String assessmentID = null;
	String sort = "name";
	String type = "asc";
	String doit = "false";
	
	public static String CONFIRM_AUTOCHOOSE = "confirmAutoChoose";
	public static String CONFIRM_UPDATE = "confirmUpdate";
	
	@Override
	public void prepare() throws Exception {
		data = new AssessmentRoomScheduleData(MessageLoggerFactory.createMessageLogger(this,true));
		roomList = new ArrayList<AssessmentRoomBean>();
	}
	
	@Override
	public String execute() throws Exception {
		return editRooms(); 
	}
	
	public String editRooms() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentRoomService.getRoomsForAssessment(data, assessmentID, "FER", 
				roomList,sort,type,getCurrentUser().getUserID()
			);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) return SUCCESS;
		
		return INPUT;
	}
	
	public String updateRooms() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentRoomService.updateRoomsForAssessment(data, assessmentID, roomList,getCurrentUser().getUserID(),doit);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_NONFATAL_ERROR)) return INPUT;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return CONFIRM_UPDATE;
	}
	
	public String autoChooseRooms() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		System.out.println(assessmentID);
		
		AssessmentRoomService.autoChooseRooms(data, assessmentID,doit,getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return CONFIRM_AUTOCHOOSE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
	}
	
	public String getAvailableStatus() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentRoomService.getAvailableStatus(data, assessmentID,getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return INPUT;
	}

	public String syncReservations() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		AssessmentRoomService.syncReservations(data, assessmentID,getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return INPUT;
	}

	public List<AssessmentRoomBean> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<AssessmentRoomBean> roomList) {
		this.roomList = roomList;
	}

	public AssessmentRoomScheduleData getData() {
		return data;
	}

	public void setData(AssessmentRoomScheduleData data) {
		this.data = data;
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
	
	
	public Map<String,String> getRoomTags() {
		
		if(tagCache==null) {
			tagCache = new LinkedHashMap<String, String>();
			List<AssessmentRoomTag> list = Arrays.asList(AssessmentRoomTag.values());
			Collections.reverse(list);
			for(AssessmentRoomTag t : list)
				tagCache.put(t.name(), getText(t.name()));
		}
		
		return tagCache;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDoit() {
		return doit;
	}

	public void setDoit(String doit) {
		this.doit = doit;
	}
}
