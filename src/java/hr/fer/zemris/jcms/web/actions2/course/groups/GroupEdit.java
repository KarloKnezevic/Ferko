package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupTreeBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.GroupEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.groups.ShowGroupTreeBuilder;

@WebClass(dataClass=GroupEditData.class)
public class GroupEdit extends Ext2ActionSupport<GroupEditData> {

	private static final long serialVersionUID = 2L;

    @WebMethodInfo(
    	dataResultMappings={
    		@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="success", registerDelayedMessages=true)
    	},
    	struts2ResultMappings={
	    	@Struts2ResultMapping(struts2Result="input",navigBuilder=ShowGroupTreeBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.editGroup"}),
	    	@Struts2ResultMapping(struts2Result="success",navigBuilder=BuilderDefault.class)
    })
    public String save() {
    	GroupTreeBrowserService.editGroupUpdate(getEntityManager(), data);
    	return null;
    }

    @WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result="input",navigBuilder=ShowGroupTreeBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.editGroup"})})
    public String groupEdit() throws Exception {
    	GroupTreeBrowserService.editGroupPrepare(getEntityManager(), data);
		return null;
    }

    @WebMethodInfo(struts2ResultMappings={@Struts2ResultMapping(struts2Result="input",navigBuilder=ShowGroupTreeBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.editGroup"})})
    public String execute() throws Exception {
    	return groupEdit();
    }

	public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}
}
