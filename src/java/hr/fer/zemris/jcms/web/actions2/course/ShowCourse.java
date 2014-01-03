package hr.fer.zemris.jcms.web.actions2.course;

import hr.fer.zemris.jcms.service2.course.CourseService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseData;

/**
 * Akcija koja popunjava naslovnu stranicu kolegija.
 * 
 * @author marcupic
 */
@WebClass(dataClass=ShowCourseData.class)
public class ShowCourse extends Ext2ActionSupport<ShowCourseData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		CourseService.getShowCourseData(getEntityManager(), data);
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
