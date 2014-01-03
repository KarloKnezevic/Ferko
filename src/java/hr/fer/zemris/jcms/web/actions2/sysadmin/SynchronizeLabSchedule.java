package hr.fer.zemris.jcms.web.actions2.sysadmin;

import hr.fer.zemris.jcms.service2.sysadmin.ComponentScheduleSyncService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeLabScheduleData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

@WebClass(dataClass=SynchronizeLabScheduleData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.syncLabSchedule"})
public class SynchronizeLabSchedule extends Ext2ActionSupport<SynchronizeLabScheduleData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml")
    public String upload() throws Exception {
    	ComponentScheduleSyncService.componentScheduleSync(getEntityManager(), data);
    	return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return input();
    }

	@WebMethodInfo
    public String input() throws Exception {
    	ComponentScheduleSyncService.prepareComponentScheduleSync(getEntityManager(), data);
    	return null;
    }

    public String getSemester() {
		return data.getSemester();
	}
    public void setSemester(String semester) {
		data.setSemester(semester);
	}
    
	public String getText() {
		return data.getText();
	}
	public void setText(String text) {
		data.setText(text);
	}

}
