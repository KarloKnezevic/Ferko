package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.GroupService;
import hr.fer.zemris.jcms.web.actions.data.GroupEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class TransferUsersFromGroup extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private GroupEditData data = null;
	private String courseInstanceID;
	private Long destinationGroupID;
	private Long sourceGroupID;
	
	@Override
	public void prepare() throws Exception {
		data = new GroupEditData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String execute() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		GroupService.transferUsersFromGroup(data, getCurrentUser().getUserID(), courseInstanceID, sourceGroupID, destinationGroupID);
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public GroupEditData getData() {
		return data;
	}
    public void setData(GroupEditData data) {
		this.data = data;
	}
    
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public Long getSourceGroupID() {
		return sourceGroupID;
	}
    public void setSourceGroupID(Long sourceGroupID) {
		this.sourceGroupID = sourceGroupID;
	}
    
    public Long getDestinationGroupID() {
		return destinationGroupID;
	}
    public void setDestinationGroupID(Long destinationGroupID) {
		this.destinationGroupID = destinationGroupID;
	}
}
