package hr.fer.zemris.jcms.web.actions2.course.groups;

import hr.fer.zemris.jcms.service2.course.groups.GroupUsersService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ChangeUsersGroupData;
import hr.fer.zemris.jcms.web.navig.builders.course.groups.ShowGroupUsersBuilder;

@WebClass(dataClass=ChangeUsersGroupData.class, 
		defaultNavigBuilder=ShowGroupUsersBuilder.class, defaultNavigBuilderIsRoot=false, 
		additionalMenuItems={"m2","Navigation.groupChange"})
public class ChangeUsersGroup extends Ext2ActionSupport<ChangeUsersGroupData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		GroupUsersService.prepareGroupChange(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo(lockPath="ml\\ci${data.lid}\\g\\g${data.mpID}")
    public String change() throws Exception {
		GroupUsersService.performGroupChange(getEntityManager(), data);
		return null;
    }
    
}
