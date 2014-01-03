package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;

public class AdminAssessmentsReserveRoomsData extends AbstractActionData {

	private String text;
	private String assessmentTag;
	private String semester;
	private String currentSemesterID;
	private List<YearSemester> allYearSemesters;
	List<AssessmentTag> allAssessmentTags = Collections.emptyList();
	
	public AdminAssessmentsReserveRoomsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<AssessmentTag> getAllAssessmentTags() {
		return allAssessmentTags;
	}
	public void setAllAssessmentTags(List<AssessmentTag> allAssessmentTags) {
		this.allAssessmentTags = allAssessmentTags;
	}
	
	public String getAssessmentTag() {
		return assessmentTag;
	}
	public void setAssessmentTag(String assessmentTag) {
		this.assessmentTag = assessmentTag;
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
    
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

}
