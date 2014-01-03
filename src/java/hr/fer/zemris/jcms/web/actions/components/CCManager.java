package hr.fer.zemris.jcms.web.actions.components;

import hr.fer.zemris.jcms.service.CourseComponentService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseComponentData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

public class CCManager extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private CourseComponentData data;
	private String courseInstanceID;
	private String componentShortName;
	
	public static final String REDIRECT_SUCCESS = "redirectSuccess";
	
	@Override
	public void prepare() throws Exception {
		data = new CourseComponentData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	@Override
	public String execute() throws Exception {
		return listComponents();
	}

	public String listComponents() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.getComponentsTree(data, courseInstanceID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return SUCCESS;
	}
	
	public String addComponent() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.addCourseComponent(data, courseInstanceID, getCurrentUser().getUserID(), componentShortName);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return REDIRECT_SUCCESS;
	}
	
	public CourseComponentData getData() {
		return data;
	}

	public void setData(CourseComponentData data) {
		this.data = data;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getComponentShortName() {
		return componentShortName;
	}

	public void setComponentShortName(String componentShortName) {
		this.componentShortName = componentShortName;
	}
}
