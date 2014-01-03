package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class ConfPreloadScoreEditData extends BaseAssessment {
	
	private List<String> availableLetters;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ConfPreloadScoreEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<String> getAvailableLetters() {
		return availableLetters;
	}
	public void setAvailableLetters(List<String> availableLetters) {
		this.availableLetters = availableLetters;
	}
}
