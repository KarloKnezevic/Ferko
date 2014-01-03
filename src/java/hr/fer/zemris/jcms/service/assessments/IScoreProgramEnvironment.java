package hr.fer.zemris.jcms.service.assessments;

import java.util.Date;
import java.util.List;

public interface IScoreProgramEnvironment {
	public static final boolean YES = true;
	public static final boolean NO = false;
	public static final boolean PASSED = true;
	public static final boolean FAILED = false;
	
	public void setPresent(boolean value);
	public void setScore(double value);
	public void setPassed(boolean value);
	
	public boolean getPresent();
	public double getScore();
	public boolean getPassed();
	
	public boolean isPresentSet();
	public boolean isScoreSet();
	public boolean isPassedSet();
	
	public double rawScore();
	public boolean rawPresent();
	
	public void execute();
	
	public double score(String assessmentShortName);
	public boolean present(String assessmentShortName);
	public boolean passed(String assessmentShortName);
	public double assessmentScore(String assessmentShortName);
	public boolean assessmentPresent(String assessmentShortName);
	public boolean assessmentPassed(String assessmentShortName);
	public boolean flagValue(String flagShortName);
	
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
	
	public void sumChildren();
}
