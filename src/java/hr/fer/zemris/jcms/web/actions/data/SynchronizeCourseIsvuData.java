package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.File;
import java.util.List;

public class SynchronizeCourseIsvuData extends AbstractActionData {

	private File archive;
	private String archiveContentType;
	private String archiveFileName;
	private String semester;
	
	private String currentSemesterID;
	private List<YearSemester> allYearSemesters;
	
	public SynchronizeCourseIsvuData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getCurrentSemesterID() {
		return currentSemesterID;
	}
	public void setCurrentSemesterID(String currentSemesterID) {
		this.currentSemesterID = currentSemesterID;
	}

	public List<YearSemester> getAllYearSemesters() {
		return allYearSemesters;
	}
	public void setAllYearSemesters(List<YearSemester> allYearSemesters) {
		this.allYearSemesters = allYearSemesters;
	}
	
    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public File getArchive() {
		return archive;
	}
    public void setArchive(File archive) {
		this.archive = archive;
	}

    public String getArchiveContentType() {
		return archiveContentType;
	}
    public void setArchiveContentType(String archiveContentType) {
		this.archiveContentType = archiveContentType;
	}

    public String getArchiveFileName() {
		return archiveFileName;
	}
    public void setArchiveFileName(String archiveFileName) {
		this.archiveFileName = archiveFileName;
	}

}
