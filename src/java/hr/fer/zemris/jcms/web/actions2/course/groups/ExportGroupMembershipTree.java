package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupExportsService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ExportGroupMembershipTreeData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;

@WebClass(dataClass=ExportGroupMembershipTreeData.class)
public class ExportGroupMembershipTree extends Ext2ActionSupport<ExportGroupMembershipTreeData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream", navigBuilder=DefaultNavigationBuilder.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))})
    public String execute() throws Exception {
		GroupExportsService.exportGroupMembers(getEntityManager(), data);
        return null;
    }

	public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}
	
	public String getFormat() {
		return data.getFormat();
	}
	public void setFormat(String format) {
		data.setFormat(format);
	}


}
