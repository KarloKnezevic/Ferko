package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.CoarseGroupStat2;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

public class GroupCoarseStatData extends AbstractActionData {

	private String parentRelativePath;
	private List<List<CoarseGroupStat2>> stats;
	private List<YearSemester> allSemesters;
	private YearSemester yearSemester;
	
	public GroupCoarseStatData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getParentRelativePath() {
		return parentRelativePath;
	}
	public void setParentRelativePath(String parentRelativePath) {
		this.parentRelativePath = parentRelativePath;
	}
	
	public List<List<CoarseGroupStat2>> getStats() {
		return stats;
	}
	public void setStats(List<List<CoarseGroupStat2>> stats) {
		this.stats = stats;
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
}
