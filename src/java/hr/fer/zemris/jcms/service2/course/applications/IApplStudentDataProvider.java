package hr.fer.zemris.jcms.service2.course.applications;

import hr.fer.zemris.jcms.service.assessments.StudentTask;

import java.util.Date;
import java.util.List;

public interface IApplStudentDataProvider {
	
	/**
	 * Pomoćna metoda koja čisti sve podatke prikupljene za konkretnog studenta,
	 * ali ostavlja opće podatke koji će možda biti korisni ako se isti objekt
	 * koristi za nekog drugog studenta.
	 */
	public void clearStudentData();

	public boolean flagValue(String flagShortName);

	public boolean passed(String assessmentShortName);

	public boolean present(String assessmentShortName);

	public double score(String assessmentShortName);
	
	public boolean assessmentPassed(String assessmentShortName);

	public boolean assessmentPresent(String assessmentShortName);

	public double assessmentScore(String assessmentShortName);

	public boolean hasApplication(String applShortName);
	
	public boolean hasApplicationInStatus(String applShortName, String status);
	
	public Date getApplicationDate(String applShortName);
	
	public boolean existsApplication(String applShortName);
	
	public boolean existsAssessment(String assessmentName);

	public boolean existsFlag(String flagName);

	public String assessmentShortNameForTag(String tagShortName);

	public String flagShortNameForTag(String tagShortName);

	public StudentTask task(String componentShortName, int position, String taskName);
	
	public List<StudentTask> tasks(String componentShortName, int position);
	
	public boolean hasAssignedTask(String componentShortName, int position, String taskName);

	public String getApplicationElementValue(String applShortName, String elementName);
}
