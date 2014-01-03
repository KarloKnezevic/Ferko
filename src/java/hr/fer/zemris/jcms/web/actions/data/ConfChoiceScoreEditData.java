package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.ConfChoiceScoreEditBean;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author Ivan Krišto
 *
 */
public class ConfChoiceScoreEditData extends BaseAssessment {
	
	private List<String> availableLetters;
	private ConfChoiceScoreEditBean bean = new ConfChoiceScoreEditBean();

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ConfChoiceScoreEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<String> getAvailableLetters() {
		return availableLetters;
	}
	public void setAvailableLetters(List<String> availableLetters) {
		this.availableLetters = availableLetters;
	}
	
    public ConfChoiceScoreEditBean getBean() {
		return bean;
	}
    public void setBean(ConfChoiceScoreEditBean bean) {
		this.bean = bean;
	}
}
