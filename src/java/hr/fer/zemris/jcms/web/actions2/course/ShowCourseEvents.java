package hr.fer.zemris.jcms.web.actions2.course;

import java.util.Date;

import hr.fer.zemris.jcms.service2.course.CourseCalendarService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseEventsData;

@WebClass(dataClass=ShowCourseEventsData.class)
public class ShowCourseEvents extends Ext2ActionSupport<ShowCourseEventsData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo
    public String execute() throws Exception {
		CourseCalendarService.getCourseSimpleCalendar(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
	public String getSDateFrom() {
		return data.getSDateFrom();
	}

	public void setSDateFrom(String dateFrom) {
		data.setSDateFrom(dateFrom);
	}

	public String getSDateTo() {
		return data.getSDateTo();
	}

	public void setSDateTo(String dateTo) {
		data.setSDateTo(dateTo);
	}

	public Date getDateFrom() {
		return data.getDateFrom();
	}

	public void setDateFrom(Date dateFrom) {
		data.setDateFrom(dateFrom);
	}

	public Date getDateTo() {
		return data.getDateTo();
	}

	public void setDateTo(Date dateTo) {
		data.setDateTo(dateTo);
	}
}
