package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import java.util.List;

public class StudentScheduleAnalyzerData extends AbstractActionData {

	private List<YearSemester> allSemesters;
	private YearSemester yearSemester;
	private String dateFrom;
	private String dateTo;
	private DeleteOnCloseFileInputStream stream;
	private CourseInstance courseInstance;
	private String jmbagsSingleLine;
	
	public StudentScheduleAnalyzerData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}
	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}
	public String getJmbagsSingleLine() {
		return jmbagsSingleLine;
	}
	public void setJmbagsSingleLine(String jmbagsSingleLine) {
		this.jmbagsSingleLine = jmbagsSingleLine;
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

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}

	public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}
	
}
