package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ActivityBean;
import hr.fer.zemris.jcms.beans.ext.ToDoBean;
import hr.fer.zemris.jcms.model.AbstractEvent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.CourseInstanceWithGroup;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Date;
import java.util.List;

public class MainData extends AbstractActionData {

	private String porukaAdmina;
	private List<AbstractEvent> events;
	private List<CourseInstanceWithGroup> courseInstanceWithGroups;
	private List<YearSemester> allSemesters;
	private YearSemester selectedSemester;
	private List<CourseInstance> allCourseInstances;
	private List<Poll> pollsForUser;
	private List<Poll> pollsForOwner;
	private String userKey;
	private boolean renderCourseAdministration;
	private boolean renderSystemAdministration;
	private List<ToDoBean> ownToDoList;
	private List<ActivityBean> activityBeans;

	private Date dateFrom;
	private Date dateTo;
	private String currentYearSemesterID;
	// Vrsta kalendara:
	// 1 - semestar
	// 2 - mjesec
	// 3 - tjedan
	// 4 - dan
	// 5 - sljedećih 7 dana
	// 6 - sve što postoji
	private int calendarType = 1;

	public MainData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getPorukaAdmina() {
		return porukaAdmina;
	}

	public void setPorukaAdmina(String porukaAdmina) {
		this.porukaAdmina = porukaAdmina;
	}

	public List<AbstractEvent> getEvents() {
		return events;
	}

	public void setEvents(List<AbstractEvent> events) {
		this.events = events;
	}

	public List<CourseInstanceWithGroup> getCourseInstanceWithGroups() {
		return courseInstanceWithGroups;
	}

	public void setCourseInstanceWithGroups(
			List<CourseInstanceWithGroup> courseInstanceWithGroups) {
		this.courseInstanceWithGroups = courseInstanceWithGroups;
	}

	public List<YearSemester> getAllSemesters() {
		return allSemesters;
	}

	public void setAllSemesters(List<YearSemester> allSemesters) {
		this.allSemesters = allSemesters;
	}

	public YearSemester getSelectedSemester() {
		return selectedSemester;
	}

	public void setSelectedSemester(YearSemester selectedSemester) {
		this.selectedSemester = selectedSemester;
	}

	public List<CourseInstance> getAllCourseInstances() {
		return allCourseInstances;
	}

	public void setAllCourseInstances(List<CourseInstance> allCourseInstances) {
		this.allCourseInstances = allCourseInstances;
	}
	
	public boolean getRenderCourseAdministration() {
		return renderCourseAdministration;
	}
	public void setRenderCourseAdministration(boolean renderCourseAdministration) {
		this.renderCourseAdministration = renderCourseAdministration;
	}

	public List<Poll> getPollsForUser() {
		return pollsForUser;
	}

	public void setPollsForUser(List<Poll> pollsForUser) {
		this.pollsForUser = pollsForUser;
	}

	public List<Poll> getPollsForOwner() {
		return pollsForOwner;
	}

	public void setPollsForOwner(List<Poll> pollsForOwner) {
		this.pollsForOwner = pollsForOwner;
	}
	
	public boolean getRenderSystemAdministration() {
		return renderSystemAdministration;
	}
	public void setRenderSystemAdministration(boolean renderSystemAdministration) {
		this.renderSystemAdministration = renderSystemAdministration;
	}

	public List<ToDoBean> getOwnToDoList() {
		return ownToDoList;
	}
	public void setOwnToDoList(List<ToDoBean> ownToDoList) {
		this.ownToDoList = ownToDoList;
	}

	public List<ActivityBean> getActivityBeans() {
		return activityBeans;
	}
	public void setActivityBeans(List<ActivityBean> activityBeans) {
		this.activityBeans = activityBeans;
	}
	
	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getCurrentYearSemesterID() {
		return currentYearSemesterID;
	}

	public void setCurrentYearSemesterID(String currentYearSemesterID) {
		this.currentYearSemesterID = currentYearSemesterID;
		if(this.currentYearSemesterID!=null && this.currentYearSemesterID.equals("")) this.currentYearSemesterID = null;
	}

	public int getCalendarType() {
		return calendarType;
	}

	public void setCalendarType(int calendarType) {
		this.calendarType = calendarType;
	}
}
