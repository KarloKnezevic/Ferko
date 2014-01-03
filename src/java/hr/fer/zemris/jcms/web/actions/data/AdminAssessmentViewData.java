package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.AssessmentConfigurationSelectorBean;

import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentViewData extends BaseAssessment {
	
	private String assessmentID;
	private List<AssessmentConfigurationSelectorBean> confSelectors;
	private String assessmentConfigurationKey;
	private List<AssessmentFile> files;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<AssessmentConfigurationSelectorBean> getConfSelectors() {
		return confSelectors;
	}
	public void setConfSelectors(
			List<AssessmentConfigurationSelectorBean> confSelectors) {
		this.confSelectors = confSelectors;
	}

	public String getAssessmentConfigurationKey() {
		return assessmentConfigurationKey;
	}
	public void setAssessmentConfigurationKey(String assessmentConfigurationKey) {
		this.assessmentConfigurationKey = assessmentConfigurationKey;
	}

	public List<AssessmentFile> getFiles() {
		return files;
	}
	public void setFiles(List<AssessmentFile> files) {
		this.files = files;
	}
	
    public String getAssessmentID() {
		return assessmentID;
	}
    public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
}
