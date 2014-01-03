package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.InputStream;
import java.util.List;

public class GroupMembershipExportData extends BaseCourseInstance {

	private List<YearSemester> allSemesters;
	private YearSemester yearSemester;
	private String mimeType;
	private String fileName;
	private long length;
	private InputStream stream;
	private List<CourseInstance> allCourses;
	
	public GroupMembershipExportData(IMessageLogger messageLogger) {
		super(messageLogger);
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

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	
	public List<CourseInstance> getAllCourses() {
		return allCourses;
	}
	public void setAllCourses(List<CourseInstance> allCourses) {
		this.allCourses = allCourses;
	}
	
}
