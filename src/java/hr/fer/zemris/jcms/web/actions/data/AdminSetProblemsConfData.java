package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.AdminSetProblemsConf;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminSetProblemsConf}.
 *  
 * @author Ivan Krišto
 *
 */
public class AdminSetProblemsConfData extends AdminAssessmentViewData {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminSetProblemsConfData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
