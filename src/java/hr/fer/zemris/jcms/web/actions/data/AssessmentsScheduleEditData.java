package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;

public class AssessmentsScheduleEditData extends AbstractActionData {
	
	List<AssessmentTag> allAssessmentTags = Collections.emptyList();;
	List<YearSemester> allSemesters = Collections.emptyList();;
	
	public AssessmentsScheduleEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<AssessmentTag> getAllAssessmentTags() {
		return allAssessmentTags;
	}

	public void setAllAssessmentTags(List<AssessmentTag> allAssessmentTags) {
		this.allAssessmentTags = allAssessmentTags;
	}

	public List<YearSemester> getAllSemesters() {
		return allSemesters;
	}

	public void setAllSemesters(List<YearSemester> allSemesters) {
		this.allSemesters = allSemesters;
	}
}
