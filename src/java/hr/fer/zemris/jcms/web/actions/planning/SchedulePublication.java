package hr.fer.zemris.jcms.web.actions.planning;

import hr.fer.zemris.jcms.service.PlanningService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.PlanningData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;

import com.opensymphony.xwork2.Preparable;

public class SchedulePublication extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private PlanningData data = null;
	private String courseInstanceID;
	private String scheduleID;
	private String publicationGroups;
	private InputStreamWrapper streamWrapper;
	
	@Override
	public void prepare() throws Exception {
		data = new PlanningData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String execute() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	PlanningService.getSchedule(data, getCurrentUser().getUserID(), courseInstanceID, scheduleID);
    	data.getMessageLogger().registerAsDelayed();
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) return SUCCESS;
    	else return "listPlans";
    } 
    
    public String prepareForPublishing() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	boolean publishImmediately = false;
    	PlanningService.checkScheduleValidity(data, getCurrentUser().getUserID(), courseInstanceID, scheduleID, publishImmediately);
    	data.getMessageLogger().registerAsDelayed();
    	if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) return "finalCheck";
    	else return "listPlans";
    }
    
    public String publish() throws Exception{
       	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
       	boolean publishImmediately = true;
    	PlanningService.checkScheduleValidity(data, getCurrentUser().getUserID(), courseInstanceID, scheduleID, publishImmediately);
    	data.getMessageLogger().registerAsDelayed();
    	return "finalCheck";
    }
    
    public String validatePublicationGroups() throws Exception{
       	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
       	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.validatePublicationGroups(getCurrentUser().getUserID(), courseInstanceID, publicationGroups, wrapper);
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
    }

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public PlanningData getData() {
		return data;
	}

	public void setData(PlanningData data) {
		this.data = data;
	}

	public String getScheduleID() {
		return scheduleID;
	}

	public void setScheduleID(String scheduleID) {
		this.scheduleID = scheduleID;
	}

	public String getPublicationGroups() {
		return publicationGroups;
	}

	public void setPublicationGroups(String publicationGroups) {
		this.publicationGroups = publicationGroups;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}



}
