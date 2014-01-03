package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupEventsService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ListGroupEventsData;

@WebClass(dataClass=ListGroupEventsData.class)
public class ListGroupEvents extends Ext2ActionSupport<ListGroupEventsData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
    	GroupEventsService.listGroupsEvents(getEntityManager(), data);
        return null;
    }

	public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}

}
