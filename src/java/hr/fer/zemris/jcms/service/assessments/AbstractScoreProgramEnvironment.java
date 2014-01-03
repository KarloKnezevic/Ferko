package hr.fer.zemris.jcms.service.assessments;

import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;


public abstract class AbstractScoreProgramEnvironment implements IScoreProgramEnvironment {

	private boolean passed;
	private boolean present;
	private double score;
	private boolean _rawPresent;
	private double _rawScore;
	private boolean passedSet;
	private boolean presentSet;
	private boolean scoreSet;
	private IScoreCalculatorEngine engine;
	private IScoreCalculatorContext context;
	private String thisAssessmentShortName;
	
	public AbstractScoreProgramEnvironment(IScoreCalculatorEngine engine, IScoreCalculatorContext context, 
			boolean _rawPresent, double _rawScore, String thisAssessmentShortName) {
		super();
		this.thisAssessmentShortName = thisAssessmentShortName;
		this._rawPresent = _rawPresent;
		this._rawScore = _rawScore;
		this.context = context;
		this.engine = engine;
	}

	@Override
	public boolean getPassed() {
		return passed;
	}

	@Override
	public boolean getPresent() {
		return present;
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public boolean isPassedSet() {
		return passedSet;
	}

	@Override
	public boolean isPresentSet() {
		return presentSet;
	}

	@Override
	public boolean isScoreSet() {
		return scoreSet;
	}

	@Override
	public boolean rawPresent() {
		return _rawPresent;
	}

	@Override
	public double rawScore() {
		return _rawScore;
	}

	@Override
	public void setPassed(boolean value) {
		passed = value;
		passedSet = true;
	}

	@Override
	public void setPresent(boolean value) {
		present = value;
		presentSet = true;
	}

	@Override
	public void setScore(double value) {
		score = value;
		scoreSet = true;
	}

	@Override
	public boolean flagValue(String flagShortName) {
		StudentFlag f = engine.calculateFlag(flagShortName, context);
		return f.getValue();
	}

	@Override
	public boolean passed(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		if(s.getEffectiveStatus()==null) return false;
		switch(s.getEffectiveStatus()) {
		case PASSED: return true;
		case FAILED: return false;
		}
		return false;
	}

	@Override
	public boolean present(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		return s.getEffectivePresent();
	}

	@Override
	public double score(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		return s.getEffectiveScore();
	}
	
	@Override
	public boolean assessmentPassed(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		if(s.getStatus()==null) return false;
		switch(s.getStatus()) {
		case PASSED: return true;
		case FAILED: return false;
		}
		return false;
	}

	@Override
	public boolean assessmentPresent(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		return s.getPresent();
	}

	@Override
	public double assessmentScore(String assessmentShortName) {
		StudentScore s = engine.calculateScore(assessmentShortName, context);
		return s.getScore();
	}
	
	@Override
	public boolean hasApplication(String applShortName) {
		StudentApplicationShortBean bean = context.getAssessmentDataProvider().getStudentApplication(applShortName, context.getCurrentUser());
		return bean!=null && bean.getStatus()!=null && bean.getStatus().length()>0;
	}
	
	@Override
	public boolean hasApplicationInStatus(String applShortName, String status) {
		StudentApplicationShortBean bean = context.getAssessmentDataProvider().getStudentApplication(applShortName, context.getCurrentUser());
		if(bean==null || bean.getStatus()==null || bean.getStatus().length()==0) return false;
		return bean.getStatus().equals(status);
	}
	
	@Override
	public String getApplicationElementValue(String applShortName, String elementName) {
		return context.getAssessmentDataProvider().getApplicationElementValue(applShortName, context.getCurrentUser(), elementName);
	}
	
	@Override
	public Date getApplicationDate(String applShortName) {
		StudentApplicationShortBean bean = context.getAssessmentDataProvider().getStudentApplication(applShortName, context.getCurrentUser());
		if(bean==null || bean.getStatus()==null || bean.getStatus().length()==0) return null;
		return bean.getDate();
	}
	
	@Override
	public boolean existsApplication(String applShortName) {
		return context.existsApplication(applShortName);
	}
	
	@Override
	public boolean existsAssessment(String assessmentName) {
		return context.existsAssessment(assessmentName);
	}

	@Override
	public boolean existsFlag(String flagName) {
		return context.existsFlag(flagName);
	}

	@Override
	public String assessmentShortNameForTag(String tagShortName) {
		return context.assessmentShortNameForTag(tagShortName);
	}

	@Override
	public String flagShortNameForTag(String tagShortName) {
		return context.flagShortNameForTag(tagShortName);
	}
	
	@Override
	public StudentTask task(String componentShortName, int position, String taskName) {
		return context.getTask(componentShortName, position, taskName, context.getCurrentUser());
	}
	
	@Override
	public List<StudentTask> tasks(String componentShortName, int position) {
		return context.getTasks(componentShortName, position, context.getCurrentUser());
	}
	
	@Override
	public boolean hasAssignedTask(String componentShortName, int position,
			String taskName) {
		return null != context.getTask(componentShortName, position, taskName, context.getCurrentUser());
	}
	
	@Override
	public void sumChildren() {
		String[] children = context.getAssessmentChildren(thisAssessmentShortName);
		if(children==null) {
			setPassed(false);
			setPresent(false);
			setScore(0);
		} else {
			double sum = 0;
			int numberOfPresent = 0;
			for(int i = 0; i < children.length; i++) {
				StudentScore s = engine.calculateScore(children[i], context);
				if(s.getEffectivePresent()) {
					numberOfPresent++;
				}
				sum += s.getEffectiveScore();
			}
			boolean pr = numberOfPresent>0;
			setPresent(pr);
			setPassed(pr);
			setScore(pr ? sum : 0.0);
		}
	}
}
