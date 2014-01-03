package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.beans.ext.GroupEventBean;
import hr.fer.zemris.jcms.service2.course.groups.GroupEventsService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.EditGroupEventData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.groups.ListGroupEventsBuilder;

@WebClass(dataClass=EditGroupEventData.class)
public class EditGroupEvent extends Ext2ActionSupport<EditGroupEventData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=ListGroupEventsBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2", "Navigation.event"})})
    public String newEvent() throws Exception {
    	GroupEventsService.newGroupsEvents(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

	@WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=ListGroupEventsBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2", "Navigation.event"})})
    public String editEvent() throws Exception {
    	GroupEventsService.editGroupsEvents(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

	@WebMethodInfo(
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=ListGroupEventsBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2", "Navigation.event"}),
			@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class)
		}
	)
    public String saveEvent() throws Exception {
    	GroupEventsService.saveOrUpdateGroupsEvents(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public GroupEventBean getBean() {
		return data.getBean();
	}
    public void setBean(GroupEventBean bean) {
		data.setBean(bean);
	}
    
	public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}
}
