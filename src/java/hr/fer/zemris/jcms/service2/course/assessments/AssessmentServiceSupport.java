package hr.fer.zemris.jcms.service2.course.assessments;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasAssessmentFlag;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

/**
 * Pomoćni razred sloja usluge koji sadrži metode za dohvat/popunjavanje podataka o
 * provjeri/zastavici te pripadnom kolegiju.
 *  
 * @author marcupic
 *
 */
public class AssessmentServiceSupport {

	/**
	 * Popunjavanje podataka o provjeri i primjerku kolegija. Ako je <code>assessmentID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ <code>Error.invalidParameters</code>, rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL},
	 * i metoda vraća false. Ako se dogodi greška kod dohvata provjere, dodaje se poruka čiji je ključ <code>Error.assessmentNotFound</code>,
	 * rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL) i metoda vraća false. Ako je sve u redu, u <code>data</code> se
	 * postavljaju i provjera i primjerak kolegija.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param assessmentID identifikator provjere
	 * @return true ako nema greške, inače false.
	 */
	public static <T extends AbstractActionData & HasAssessment & HasCourseInstance> boolean fillAssessment(EntityManager em, T data, String assessmentID) {
		if(assessmentID==null || assessmentID.length()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		Assessment assessment = null;
		try {
			assessment = DAOHelperFactory.getDAOHelper().getAssessmentDAO().get(em, Long.valueOf(assessmentID));
		} catch(Exception ignorable) {
		}
		if(assessment==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setAssessment(assessment);
		data.setCourseInstance(assessment.getCourseInstance());
		return true;
	}

	/**
	 * Popunjavanje podataka o zastavici i primjerku kolegija. Ako je <code>assessmentFlagID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ <code>Error.invalidParameters</code>, rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL},
	 * i metoda vraća false. Ako se dogodi greška kod dohvata zastavice, dodaje se poruka čiji je ključ <code>Error.assessmentFlagNotFound</code>,
	 * rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL) i metoda vraća false. Ako je sve u redu, u <code>data</code> se
	 * postavljaju i zastavica i primjerak kolegija.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param assessmentFlagID identifikator zastavice
	 * @return true ako nema greške, inače false.
	 */
	public static <T extends AbstractActionData & HasAssessmentFlag & HasCourseInstance> boolean fillAssessmentFlag(EntityManager em, T data, String assessmentFlagID) {
		if(assessmentFlagID==null || assessmentFlagID.length()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		AssessmentFlag assessmentFlag = null;
		try {
			assessmentFlag = DAOHelperFactory.getDAOHelper().getAssessmentDAO().getFlag(em, Long.valueOf(assessmentFlagID));
		} catch(Exception ignorable) {
		}
		if(assessmentFlag==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.assessmentFlagNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setAssessmentFlag(assessmentFlag);
		data.setCourseInstance(assessmentFlag.getCourseInstance());
		return true;
	}

}
