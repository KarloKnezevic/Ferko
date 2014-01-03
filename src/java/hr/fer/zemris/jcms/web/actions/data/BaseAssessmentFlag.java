package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AssessmentFlag;

import hr.fer.zemris.jcms.service.has.HasAssessmentFlag;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class BaseAssessmentFlag extends BaseCourseInstance implements HasAssessmentFlag {
	
	private AssessmentFlag assessmentFlag;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public BaseAssessmentFlag(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	/**
	 * Zastavica koja je upravo uređena/stvorena. Dok akcija nije uspješna
	 * (za slučaj stvaranja zastavice), ovo će biti null; kod uređivanja, bit će
	 * postavljeno ako je moguće.
	 * 
	 * @return provjeru
	 */
	public AssessmentFlag getAssessmentFlag() {
		return assessmentFlag;
	}
	public void setAssessmentFlag(AssessmentFlag assessmentFlag) {
		this.assessmentFlag = assessmentFlag;
	}
}
