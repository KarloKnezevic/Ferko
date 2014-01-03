package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupUsersService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.UploadStudentTagsData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=UploadStudentTagsData.class, defaultNavigBuilder=BuilderDefault.class)
public class UploadStudentTags extends Ext2ActionSupport<UploadStudentTagsData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${data.lid}\\g\\g${mpID}")
    public String execute() throws Exception {
		GroupUsersService.updateGroupUserTags(getEntityManager(), data);
        return null;
    }
    
	public Long getGroupID() {
		return data.getGroupID();
	}
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}

	public String getText() {
		return data.getText();
	}
	public void setText(String text) {
		data.setText(text);
	}

	public Long getMpID() {
		return data.getMpID();
	}
	public void setMpID(Long mpID) {
		data.setMpID(mpID);
	}
	
}
