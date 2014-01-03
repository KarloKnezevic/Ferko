package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationListStudentsData;

@WebClass(dataClass=ApplicationListStudentsData.class)
public class ApplicationListStudents extends Ext2ActionSupport<ApplicationListStudentsData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
    	ApplicationService.listStudentsOnApplication(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
	
    public Long getDefinitionID() {
		return data.getDefinitionID();
	}

	public void setDefinitionID(Long definitionID) {
		data.setDefinitionID(definitionID);
	}
    
}
