package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.AssessmentCreateAppeal;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AssessmentCreateAppeal}.
 *  
 * @author Ivan Krišto
 *
 */
public class AssessmentCreateAppealData extends BaseAssessment {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentCreateAppealData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
