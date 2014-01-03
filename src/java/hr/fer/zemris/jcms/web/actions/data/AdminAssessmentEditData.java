package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.AssessmentBean;

import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentEditData extends BaseAssessment {
	
	private AssessmentBean bean = new AssessmentBean();
	private List<AssessmentTag> tags = Collections.emptyList();
	private List<AssessmentFlag> flags = Collections.emptyList();
	private List<Assessment> possibleParents = Collections.emptyList();
	private List<Assessment> possibleChainedParents = Collections.emptyList();
	private Assessment assessment;
	private List<StringNameStringValue> visibilities;
	private List<Assessment> possibleScoreSources = Collections.emptyList();
	private String guiConfig;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<AssessmentTag> getTags() {
		return tags;
	}
	public void setTags(List<AssessmentTag> tags) {
		this.tags = tags;
	}

	public List<AssessmentFlag> getFlags() {
		return flags;
	}
	public void setFlags(List<AssessmentFlag> flags) {
		this.flags = flags;
	}

	public List<Assessment> getPossibleParents() {
		return possibleParents;
	}
	public void setPossibleParents(List<Assessment> possibleParents) {
		this.possibleParents = possibleParents;
	}

	public List<Assessment> getPossibleChainedParents() {
		return possibleChainedParents;
	}
	public void setPossibleChainedParents(List<Assessment> possibleChainedParents) {
		this.possibleChainedParents = possibleChainedParents;
	}
	
	/**
	 * Provjera znanja koja je upravo uređena/stvorena. Dok akcija nije uspješna
	 * (za slučajstvaranja provjere), ovo će biti null; kod uređivanja, bit će
	 * postavljeno ako je moguće.
	 * 
	 * @return provjeru
	 */
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}
	
	public List<StringNameStringValue> getVisibilities() {
		return visibilities;
	}
	public void setVisibilities(List<StringNameStringValue> visibilities) {
		this.visibilities = visibilities;
	}
	
    public AssessmentBean getBean() {
		return bean;
	}
    public void setBean(AssessmentBean bean) {
		this.bean = bean;
	}

    public List<Assessment> getPossibleScoreSources() {
		return possibleScoreSources;
	}
    public void setPossibleScoreSources(List<Assessment> possibleScoreSources) {
		this.possibleScoreSources = possibleScoreSources;
	}
    
    public String getGuiConfig() {
    	if(guiConfig==null) return "";
		return guiConfig;
	}
    public void setGuiConfig(String guiConfig) {
		this.guiConfig = guiConfig;
	}
}
