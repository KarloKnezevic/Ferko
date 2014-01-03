package hr.fer.zemris.jcms.service2.course.applications;

import hr.fer.zemris.jcms.service.assessments.StudentTask;

import java.util.Date;
import java.util.List;

/**
 * Pomoćna implementacija koja se može koristiti za izgradnju programske prijave
 * gdje sigurno nije potrebno dohvaćati bilo kakve podatke o studentima.
 * 
 * @author marcupic
 *
 */
public class EmptyStudentDataProviderImpl implements IApplStudentDataProvider {

	@Override
	public boolean assessmentPassed(String assessmentShortName) {
		return false;
	}

	@Override
	public boolean assessmentPresent(String assessmentShortName) {
		return false;
	}

	@Override
	public double assessmentScore(String assessmentShortName) {
		return 0;
	}

	@Override
	public String assessmentShortNameForTag(String tagShortName) {
		return null;
	}

	@Override
	public void clearStudentData() {
	}

	@Override
	public boolean existsApplication(String applShortName) {
		return false;
	}

	@Override
	public boolean existsAssessment(String assessmentName) {
		return false;
	}

	@Override
	public boolean existsFlag(String flagName) {
		return false;
	}

	@Override
	public String flagShortNameForTag(String tagShortName) {
		return null;
	}

	@Override
	public boolean flagValue(String flagShortName) {
		return false;
	}

	@Override
	public Date getApplicationDate(String applShortName) {
		return null;
	}

	@Override
	public boolean hasApplication(String applShortName) {
		return false;
	}

	@Override
	public boolean hasApplicationInStatus(String applShortName, String status) {
		return false;
	}

	@Override
	public boolean hasAssignedTask(String componentShortName, int position,
			String taskName) {
		return false;
	}

	@Override
	public boolean passed(String assessmentShortName) {
		return false;
	}

	@Override
	public boolean present(String assessmentShortName) {
		return false;
	}

	@Override
	public double score(String assessmentShortName) {
		return 0;
	}

	@Override
	public StudentTask task(String componentShortName, int position,
			String taskName) {
		return null;
	}

	@Override
	public List<StudentTask> tasks(String componentShortName, int position) {
		return null;
	}

	@Override
	public String getApplicationElementValue(String applShortName,
			String elementName) {
		return null;
	}
}
