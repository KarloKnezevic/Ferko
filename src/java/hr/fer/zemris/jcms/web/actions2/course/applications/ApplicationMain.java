package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationMainData;

@WebClass(dataClass=ApplicationMainData.class)
public class ApplicationMain extends Ext2ActionSupport<ApplicationMainData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
    	ApplicationService.retrieveApplicationDefinitions(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
}
