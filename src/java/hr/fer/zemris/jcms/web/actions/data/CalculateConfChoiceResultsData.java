package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.CalculateConfChoiceResults;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link CalculateConfChoiceResults}.
 *  
 * @author Ivan Krišto
 *
 */
public class CalculateConfChoiceResultsData extends AdminAssessmentViewData {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CalculateConfChoiceResultsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
