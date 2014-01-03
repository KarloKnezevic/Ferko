package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupUsersService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.TransferUsersFromGroupData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=TransferUsersFromGroupData.class, defaultNavigBuilder=BuilderDefault.class)
public class TransferUsersFromGroup extends Ext2ActionSupport<TransferUsersFromGroupData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${data.lid}\\g\\g${mpID}")
    public String execute() throws Exception {
		GroupUsersService.performGroupTransfer(getEntityManager(), data);
		return null;
    }

    public Long getSourceGroupID() {
		return data.getSourceGroupID();
	}
    public void setSourceGroupID(Long sourceGroupID) {
		data.setSourceGroupID(sourceGroupID);
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
