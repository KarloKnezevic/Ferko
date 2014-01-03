package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class ImportCourseMPConstraintsData extends AbstractActionData {

	private String currentSemesterID;
	private List<YearSemester> allYearSemesters;
	
	public ImportCourseMPConstraintsData(IMessageLogger messageLogger) {
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
}
