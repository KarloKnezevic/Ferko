package hr.fer.zemris.jcms.service2.course.assessments;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Pomoćne metode vezane uz provjere.
 * 
 * @author marcupic
 *
 */
public class AssessmentsUtil {

	/**
	 * Dohvaća sortiranu listu provjera na kolegiju.
	 * 
	 * @param em entity manager
	 * @param courseInstance kolegij
	 * @return sortirana lista provjera
	 */
	public static List<Assessment> getSortedCourseInstanceAssessments(EntityManager em, CourseInstance courseInstance) {
		// TODO: sortiraj ovo abecedno unutar istog indeksa provjere, ali inače po indeksima
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		List<Assessment> alist = dh.getAssessmentDAO().listForCourseInstance(em, courseInstance.getId());
		Collections.sort(alist,new Comparator<Assessment>() {
			@Override
			public int compare(Assessment o1, Assessment o2) {
				return StringUtil.HR_COLLATOR.compare(o1.getShortName(), o2.getShortName());
			}
		});
		return alist;
	}

	/**
	 * Dohvaća sortiranu listu zastavica na kolegiju.
	 * @param em entity manager
	 * @param courseInstance kolegij
	 * @return sortirana lista zastavica
	 */
	public static List<AssessmentFlag> getSortedCourseInstanceAssessmentFlags(EntityManager em, CourseInstance courseInstance) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		List<AssessmentFlag> flist = dh.getAssessmentDAO().listFlagsForCourseInstance(em, courseInstance.getId());
		Collections.sort(flist,new Comparator<AssessmentFlag>() {
			@Override
			public int compare(AssessmentFlag o1, AssessmentFlag o2) {
				return StringUtil.HR_COLLATOR.compare(o1.getShortName(), o2.getShortName());
			}
		});
		return flist;
	}
}
