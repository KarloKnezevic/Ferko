package hr.fer.zemris.jcms.service2.course;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

/**
 * Pomoćni razred sloja usluge koji sadrži metode za dohvat/popunjavanje podataka o
 * primjerku kolegija.
 *  
 * @author marcupic
 *
 */
public class CourseInstanceServiceSupport {

	/**
	 * Popunjavanje podataka o primjerku kolegija. Ako je <code>courseInstanceID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ određen argumentom <code>message</code>, rezultat postavlja na onaj određen parametrom <code>result</code>,
	 * i metoda vraća false. Ako se dogodi greška kod dohvata provjere, postupa se na jednak način. Ako je sve u redu, u <code>data</code> se
	 * postavljaju i provjera i primjerak kolegija.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param message ključ poruke za slučaj pogreške
	 * @param result rezultat koji treba postaviti u slučaju pogreške
	 * @return true ako nema pogreške, inače false.
	 */
	public static <T extends AbstractActionData & HasCourseInstance> boolean fillCourseInstance(EntityManager em, T data, String courseInstanceID, String message, String result) {
		CourseInstance ci = null;
		if(courseInstanceID!=null && courseInstanceID.length()>0) {
			ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, courseInstanceID);
		}
		if(ci==null) {
			if(message!=null) data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(message));
			if(result!=null) data.setResult(result);
			return false;
		}
		data.setCourseInstance(ci);
		return true;
	}

	/**
	 * Popunjavanje podataka o primjerku kolegija. Ako je <code>courseInstanceID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ <code>Error.invalidParameters</code>, rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL},
	 * i metoda vraća false. Ako se dogodi greška kod dohvata provjere, dodaje se poruka čiji je ključ <code>Error.courseInstanceNotFound</code>,
	 * rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL) i metoda vraća false. Ako je sve u redu, u <code>data</code> se
	 * postavlja primjerak kolegija.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @return true ako nema pogreške, inače false.
	 */
	public static <T extends AbstractActionData & HasCourseInstance> boolean fillCourseInstance(EntityManager em, T data, String courseInstanceID) {
		CourseInstance ci = null;
		if(courseInstanceID==null || courseInstanceID.length()<=0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, courseInstanceID);
		if(ci==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.courseInstanceNotFound"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setCourseInstance(ci);
		return true;
	}

}
