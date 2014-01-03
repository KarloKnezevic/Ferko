package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupTreeBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ShowGroupTree2Data;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.groups.ShowGroupTreeBuilder;

@WebClass(dataClass=ShowGroupTree2Data.class)
public class ShowGroupTree extends Ext2ActionSupport<ShowGroupTree2Data> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
	public String execute() throws Exception {
		GroupTreeBrowserService.fetchGroupTree(getEntityManager(), data);
		data.setTreeAsJSON(GroupTreeBrowserService.convertGroupTreeToJSON(data));
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result="addSubgroups",registerDelayedMessages=false)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="addSubgroups",navigBuilder=ShowGroupTreeBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.newGroupsAddition"})}
	)
	public String newSubgroupsPrepare() throws Exception {
		GroupTreeBrowserService.newSubgroupsPrepare(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result="addSubgroups",registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="addSubgroups",navigBuilder=ShowGroupTreeBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.newGroupsAddition"}),
			@Struts2ResultMapping(struts2Result="redirect",navigBuilder=BuilderDefault.class)
		}
	)
	public String newSubgroupsAdd() throws Exception {
		GroupTreeBrowserService.newSubgroupsAdd(getEntityManager(), data);
		return null;
	}
    
    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
}
