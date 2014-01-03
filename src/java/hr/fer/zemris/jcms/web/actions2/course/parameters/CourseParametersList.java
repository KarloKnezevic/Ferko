package hr.fer.zemris.jcms.web.actions2.course.parameters;

import hr.fer.zemris.jcms.service2.course.parameters.CourseParametersService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.CourseParametersListData;

@WebClass(dataClass=CourseParametersListData.class)
public class CourseParametersList extends Ext2ActionSupport<CourseParametersListData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		CourseParametersService.showCourseParametersList(getEntityManager(), data);
        return null;
    }

    /**
     * Geter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @return identifikator kolegija
     */
    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    /**
     * Seter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @param courseInstanceID identifikator kolegija
     */
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
}
