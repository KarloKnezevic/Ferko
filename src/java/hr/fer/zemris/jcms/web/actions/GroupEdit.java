package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.service.GroupService;
import hr.fer.zemris.jcms.web.actions.data.GroupEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class GroupEdit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private GroupEditData data = null;
	private String courseInstanceID;
	private GroupBean bean = new GroupBean();
	private Long parentGroupID;
	private Long groupID;
	
	@Override
	public void prepare() throws Exception {
		data = new GroupEditData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String newGroupInput() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		GroupService.getNewGroupInputData(data, getCurrentUser().getUserID(), courseInstanceID, parentGroupID, bean);
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String save() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	if(bean.getId()==null) {
    		GroupService.getNewGroupCreateData(data, getCurrentUser().getUserID(), courseInstanceID, parentGroupID, bean);
    	} else {
    		GroupService.getGroupSaveData(data, getCurrentUser().getUserID(), courseInstanceID, parentGroupID, bean);
    	}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String groupEdit() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		GroupService.getGroupEditData(data, getCurrentUser().getUserID(), courseInstanceID, groupID, bean);
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return newGroupInput();
    }

    public GroupEditData getData() {
		return data;
	}
    public void setData(GroupEditData data) {
		this.data = data;
	}
    
    public GroupBean getBean() {
		return bean;
	}
    public void setBean(GroupBean bean) {
		this.bean = bean;
	}
    
    public void setParentGroupID(Long parentGroupID) {
		this.parentGroupID = parentGroupID;
	}
    public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
    public Long getParentGroupID() {
		return parentGroupID;
	}
    public Long getGroupID() {
		return groupID;
	}
    
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
