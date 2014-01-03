package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.AdminUploadProblemsConf;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminUploadProblemsConf}.
 *  
 * @author marcupic
 *
 */
public class AdminUploadProblemsConfData extends AdminAssessmentViewData {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminUploadProblemsConfData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
