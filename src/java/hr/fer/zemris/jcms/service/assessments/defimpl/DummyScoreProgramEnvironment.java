package hr.fer.zemris.jcms.service.assessments.defimpl;

import hr.fer.zemris.jcms.service.assessments.AbstractScoreProgramEnvironment;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;

public class DummyScoreProgramEnvironment extends AbstractScoreProgramEnvironment {

	public DummyScoreProgramEnvironment(IScoreCalculatorEngine engine,
			IScoreCalculatorContext context, boolean present, double score, String thisAssessmentShortName) {
		super(engine, context, present, score, thisAssessmentShortName);
	}

	@Override
	public void execute() {
		setPassed(rawScore()>=5);
		setPresent(rawPresent());
		setScore(rawScore());
	}

}
