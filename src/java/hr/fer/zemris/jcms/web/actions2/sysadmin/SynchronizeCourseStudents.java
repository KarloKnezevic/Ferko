package hr.fer.zemris.jcms.web.actions2.sysadmin;

import hr.fer.zemris.jcms.service2.sysadmin.CourseEnrollmentSyncService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseStudentsData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

@WebClass(dataClass=SynchronizeCourseStudentsData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.syncCourseStudents"})
public class SynchronizeCourseStudents extends Ext2ActionSupport<SynchronizeCourseStudentsData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml")
    public String upload() throws Exception {
		CourseEnrollmentSyncService.syncCourseEnrollment(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return input();
    }

	@WebMethodInfo
    public String input() throws Exception {
		CourseEnrollmentSyncService.prepareCourseEnrollmentSync(getEntityManager(), data);
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
