package hr.fer.zemris.jcms.web.actions.components;

import hr.fer.zemris.jcms.beans.ext.ComponentItemAssessmentBean;
import hr.fer.zemris.jcms.service.CourseComponentService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseComponentData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;

import com.opensymphony.xwork2.Preparable;

public class CCIAManager extends ExtendedActionSupport implements Preparable {
	
	private static final long serialVersionUID = 2L;

	private CourseComponentData data;
	private String id;
	private String courseComponentItemID;
	private String userID;
	private InputStreamWrapper streamWrapper;
	
	private ComponentItemAssessmentBean bean;
	
	public static final String REDIRECT_ITEM = "redirectItem";
	public static final String SHOW_MATRIX = "showMatrix";
	
	@Override
	public void prepare() throws Exception {
		data = new CourseComponentData(MessageLoggerFactory.createMessageLogger(this, true));
		bean = new ComponentItemAssessmentBean();
	}

	public String newItemAssessment() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.newItemAssessment(data, courseComponentItemID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String editItemAssessment() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.editItemAssessment(data, id, bean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String saveItemAssessment() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveItemAssessment(data, courseComponentItemID, bean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_ITEM;
	}
	
	public String autoAssign() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.autoAssignItemAssessment(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_ITEM;
	}
	
	public String showMatrix() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.showMatrix(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		return SHOW_MATRIX;
	}
	
	public String matrixAddItem() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		CourseComponentService.matrixManipulateItem(data, id, userID, getCurrentUser().getUserID(), "add", wrapper);
		streamWrapper = wrapper[0];
		return "wrapped-stream";
	}
	
	public String matrixRemoveItem() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		CourseComponentService.matrixManipulateItem(data, id, userID, getCurrentUser().getUserID(), "remove", wrapper);
		streamWrapper = wrapper[0];
		return "wrapped-stream";
	}
	
	public CourseComponentData getData() {
		return data;
	}

	public void setData(CourseComponentData data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCourseComponentItemID() {
		return courseComponentItemID;
	}

	public void setCourseComponentItemID(String courseComponentItemID) {
		this.courseComponentItemID = courseComponentItemID;
	}

	public ComponentItemAssessmentBean getBean() {
		return bean;
	}

	public void setBean(ComponentItemAssessmentBean bean) {
		this.bean = bean;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
}
