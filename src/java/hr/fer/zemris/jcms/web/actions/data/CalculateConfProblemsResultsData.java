package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.CalculateConfProblemsResults;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link CalculateConfProblemsResults}.
 *  
 * @author Ivan Krišto
 *
 */
public class CalculateConfProblemsResultsData extends AdminAssessmentViewData {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CalculateConfProblemsResultsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
