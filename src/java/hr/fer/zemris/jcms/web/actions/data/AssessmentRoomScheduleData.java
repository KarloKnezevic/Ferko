package hr.fer.zemris.jcms.web.actions.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.AssessmentRoomBean;
import hr.fer.zemris.jcms.model.extra.AssessmentRoomTag;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class AssessmentRoomScheduleData extends BaseAssessment {

	private String assessmentID = null;
	private List<AssessmentRoomBean> roomList = new ArrayList<AssessmentRoomBean>();
	private Map<String,String> tagCache;
	private String sort = "name";
	private String type = "asc";
	private String doit = "false";
	private String roomName;
	private String roomVenue;
	
	private int userNumber;
	private int currCapacity;
	private String percent;
	
	
	public AssessmentRoomScheduleData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public String getRoomVenue() {
		return roomVenue;
	}
	public void setRoomVenue(String roomVenue) {
		this.roomVenue = roomVenue;
	}
	
	public int getCurrCapacity() {
		return currCapacity;
	}

	public void setCurrCapacity(int currCapacity) {
		this.currCapacity = currCapacity;
	}

	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}
	
	public int getUserNumber() {
		return userNumber;
	}
	
	public String getPercent() {
		return percent;
	}
	
	public void setPercent(String percent) {
		this.percent = percent;
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
	
	public Map<String,String> getRoomTags() {
		if(tagCache==null) {
			tagCache = new LinkedHashMap<String, String>();
			List<AssessmentRoomTag> list = Arrays.asList(AssessmentRoomTag.values());
			Collections.reverse(list);
			for(AssessmentRoomTag t : list)
				tagCache.put(t.name(), getMessageLogger().getText(t.name()));
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
