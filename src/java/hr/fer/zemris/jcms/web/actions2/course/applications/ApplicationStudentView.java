package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentViewData;
import hr.fer.zemris.jcms.web.navig.builders.course.applications.ApplicationMainBuilder;

@WebClass(dataClass=ApplicationStudentViewData.class, defaultNavigBuilder=ApplicationMainBuilder.class, defaultNavigBuilderIsRoot=false, additionalMenuItems={"m2", "Navigation.applicationEditing"})
public class ApplicationStudentView extends Ext2ActionSupport<ApplicationStudentViewData> {

	private static final long serialVersionUID = 3L;

	@WebMethodInfo
    public String execute() throws Exception {
    	ApplicationService.getStudentApplication(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

    public Long getApplicationID() {
		return data.getApplicationID();
	}
    public void setApplicationID(Long applicationID) {
		data.setApplicationID(applicationID);
	}

}
