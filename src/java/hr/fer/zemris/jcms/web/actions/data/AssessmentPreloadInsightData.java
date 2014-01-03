package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AssessmentView}.
 *  
 * @author marcupic
 *
 */
public class AssessmentPreloadInsightData extends AssessmentViewData {
	private List<AssessmentAppealInstance> userAppeals;
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentPreloadInsightData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	public void setUserAppeals(List<AssessmentAppealInstance> userAppeals) {
		this.userAppeals = userAppeals;
	}
	public List<AssessmentAppealInstance> getUserAppeals() {
		return userAppeals;
	}
}
