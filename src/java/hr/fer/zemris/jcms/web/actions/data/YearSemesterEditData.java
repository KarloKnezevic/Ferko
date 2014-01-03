package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class YearSemesterEditData extends AbstractActionData {

	private List<YearSemester> allYearSemesters;
	
	public YearSemesterEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<YearSemester> getAllYearSemesters() {
		return allYearSemesters;
	}
	public void setAllYearSemesters(List<YearSemester> allYearSemesters) {
		this.allYearSemesters = allYearSemesters;
	}
}
