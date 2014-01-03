package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.MPGSVCourse;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MPGroupSettingsViewData extends AbstractActionData {

	private String parentRelativePath;
	private List<MPGSVCourse> courses;
	private List<YearSemester> allSemesters;
	private YearSemester yearSemester;
	private String semesterID;
	private Set<Long> selectedMarketPlaces = new HashSet<Long>();
	private String ids;
	
	public MPGroupSettingsViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getParentRelativePath() {
		return parentRelativePath;
	}
	public void setParentRelativePath(String parentRelativePath) {
		this.parentRelativePath = parentRelativePath;
	}

	public List<MPGSVCourse> getCourses() {
		return courses;
	}
	public void setCourses(List<MPGSVCourse> courses) {
		this.courses = courses;
	}
	
	public List<YearSemester> getAllSemesters() {
		return allSemesters;
	}
	public void setAllSemesters(List<YearSemester> allSemesters) {
		this.allSemesters = allSemesters;
	}

	public YearSemester getYearSemester() {
		return yearSemester;
	}
	public void setYearSemester(YearSemester yearSemester) {
		this.yearSemester = yearSemester;
	}
	
    public String getSemesterID() {
		return semesterID;
	}
    public void setSemesterID(String semesterID) {
		this.semesterID = semesterID;
	}
    
    public Set<Long> getSelectedMarketPlaces() {
		return selectedMarketPlaces;
	}
    public void setSelectedMarketPlaces(Set<Long> selectedMarketPlaces) {
		this.selectedMarketPlaces = selectedMarketPlaces;
	}

    public String getIds() {
		return ids;
	}
    public void setIds(String ids) {
		this.ids = ids;
	}
}
