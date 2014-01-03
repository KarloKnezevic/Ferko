package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.User;

import java.util.Set;

public interface IAssessmentDataProvider {

	public Assessment getAssessmentByShortName(String assessmentShortName);

	public AssessmentScore getAssessmentScore(Assessment assessment, User user);

	public AssessmentFlag getAssessmentFlagByShortName(String flagShortName);

	public AssessmentFlagValue getAssessmentFlagValue(AssessmentFlag assessmentFlag, User user);

	public Set<Assessment> getKnownAssessments();

	public Set<AssessmentFlag> getKnownAssessmentFlags();
	
	public void installOnDemandApplicationsDataCallback(IOnDemandApplicationsDataCallback callback);

	public void installOnDemandStudentTaskCallback(IOnDemandStudentTaskCallback callback);

	public StudentApplicationShortBean getStudentApplication(String shortName, User user);

	public boolean existsApplication(String applShortName);
	
	public String assessmentShortNameForTag(String tagShortName);
	
	public String flagShortNameForTag(String tagShortName);
	
	public TaskData getAllStudentTaskData(String componentShortName, int itemPosition);

	public String getApplicationElementValue(String applShortName, User user, String elementName);

}
