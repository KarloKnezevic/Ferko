package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;

import java.util.Date;
import java.util.List;

public abstract class AbstractFlagProgramEnvironment implements IFlagProgramEnvironment {

	private boolean value;
	private boolean _overrideSet;
	private boolean _overrideValue;
	private boolean valueSet;
	private IScoreCalculatorEngine engine;
	private IScoreCalculatorContext context;
	
	public AbstractFlagProgramEnvironment(IScoreCalculatorEngine engine, IScoreCalculatorContext context, 
			boolean _overrideSet, boolean _overrideValue) {
		super();
		this._overrideSet = _overrideSet;
		this._overrideValue = _overrideValue;
		this.context = context;
		this.engine = engine;
	}

	@Override
	public boolean getValue() {
		return value;
	}
	
	@Override
	public boolean isValueSet() {
		return valueSet;
	}

	@Override
	public boolean overrideSet() {
		return _overrideSet;
	}

	@Override
	public boolean overrideValue() {
		return _overrideValue;
	}

	@Override
	public void setValue(boolean value) {
		valueSet = true;
		this.value = value;
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
}
