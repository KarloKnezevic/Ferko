package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupUsersService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.UpdateUserGroupMembershipData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=UpdateUserGroupMembershipData.class, defaultNavigBuilder=BuilderDefault.class)
public class UpdateUserGroupMembership extends Ext2ActionSupport<UpdateUserGroupMembershipData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${data.lid}\\g\\g${mpID}")
    public String execute() throws Exception {
    	GroupUsersService.addUsersToGroup(getEntityManager(), data);
    	return null;
    }

    public String getText() {
    	return data.getText();
    }
    public void setText(String text) {
		data.setText(text);
	}

    public boolean getRemoveOther() {
		return data.isRemoveOther();
	}
    public void setRemoveOther(boolean removeOther) {
		data.setRemoveOther(removeOther);
	}

    public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}
	
	public Long getMpID() {
		return data.getMpID();
	}
	public void setMpID(Long mpID) {
		data.setMpID(mpID);
	}

}
