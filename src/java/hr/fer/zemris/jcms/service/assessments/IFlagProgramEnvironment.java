package hr.fer.zemris.jcms.service.assessments;

import java.util.Date;
import java.util.List;

public interface IFlagProgramEnvironment {
	public static final boolean YES = true;
	public static final boolean NO = false;
	
	public void setValue(boolean value);

	public boolean getValue();
	public boolean isValueSet();
	
	public boolean overrideValue();
	public boolean overrideSet();
	
	public void execute();
	
	public double score(String assessmentShortName);
	public boolean present(String assessmentShortName);
	public boolean passed(String assessmentShortName);
	public boolean flagValue(String flagShortName);
	public double assessmentScore(String assessmentShortName);
	public boolean assessmentPresent(String assessmentShortName);
	public boolean assessmentPassed(String assessmentShortName);
	
	public boolean hasApplicationInStatus(String applShortName, String status);
	public boolean hasApplication(String applShortName);
	public Date getApplicationDate(String applShortName);
	public String getApplicationElementValue(String applShortName, String elementName);

	public boolean existsApplication(String applShortName);
	public boolean existsAssessment(String assessmentShortName);
	public boolean existsFlag(String flagShortName);
	
	public String assessmentShortNameForTag(String tagShortName);
	public String flagShortNameForTag(String tagShortName);
	
	public List<StudentTask> tasks(String componentShortName, int position);
	public StudentTask task(String componentShortName, int position, String taskName);
	public boolean hasAssignedTask(String componentShortName, int position, String taskName);
}
