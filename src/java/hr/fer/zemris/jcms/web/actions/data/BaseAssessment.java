package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Assessment;

import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class BaseAssessment extends BaseCourseInstance implements HasAssessment {
	
	protected Assessment assessment;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public BaseAssessment(IMessageLogger messageLogger) {
		super(messageLogger);
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
}
