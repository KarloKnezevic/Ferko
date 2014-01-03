package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.AssessmentTag;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pomocni razred za manipuliranje kolekcijama objekata tipa Assessment.
 * 
 * @author marcupic
 *
 */
public class AssessmentUtil {

	/**
	 * Stvara mapu provjera po isvu sifri kolegija. Pretpostavka je da izvorna kolekcija
	 * ne sadrzi dvije ili vise provjera s istog kolegija.
	 * @param assessments
	 * @return
	 */
	public static Map<String, Assessment> mapSingleAssessmentByISVUCode(Collection<Assessment> assessments) {
		Map<String, Assessment> m = new HashMap<String, Assessment>(assessments.size());
		for(Assessment a : assessments) {
			if( null != m.put(a.getCourseInstance().getCourse().getIsvuCode(), a)) {
				throw new IllegalArgumentException("Given collection contained more than one assessment per course instance.");
			}
		}
		return m;
	}

	/**
	 * U kolekciji provjera pronalazi onu koja ima zadani tag.
	 * @param assessments kolekcija provjera
	 * @param tag tag koji se trazi
	 * @return provjeru sa zadanim tagom ili null ako takva ne postoji
	 */
	public static Assessment getAssessmentWithTag(Collection<Assessment> assessments, AssessmentTag tag) {
		if (assessments == null) {
			return null;
		}
		for (Assessment a : assessments) {
			if (tag.equals(a.getAssessmentTag())) {
				return a;
			}
		}
		return null;
	}
	
	/**
	 * U kolekciji provjera pronalazi onu koja ima zadani id.
	 * @param assessments kolekcija provjera
	 * @param id id koji se trazi
	 * @return provjeru sa zadanim id-om ili null ako takva ne postoji
	 */
	public static Assessment getAssessmentWithID(Collection<Assessment> assessments, Long id) {
		if (assessments == null || id == null) {
			return null;
		}
		for (Assessment a : assessments) {
			if (id.equals(a.getId())) {
				return a;
			}
		}
		return null;
	}

	/**
	 * U kolekciji rezultata provjera pronalazi onu koja ima zadani id provjere.
	 * @param assessmentScores kolekcija rezultata provjera
	 * @param id id provjere koji se trazi
	 * @return rezultat provjere sa zadanim id-om ili null ako takav ne postoji
	 */
	public static AssessmentScore getAssessmentScoreWithID(Collection<AssessmentScore> assessmentScores, Long id) {
		if (assessmentScores == null || id == null) {
			return null;
		}
		for (AssessmentScore a : assessmentScores) {
			if (id.equals(a.getAssessment().getId())) {
				return a;
			}
		}
		return null;
	}

	/**
	 * U kolekciji zastavica pronalazi onu koja ima zadani id.
	 * @param assessmentFlags kolekcija zastavica
	 * @param id id koji se trazi
	 * @return zastavicu sa zadanim id-om ili null ako takva ne postoji
	 */
	public static AssessmentFlag getAssessmentFlagWithID(Collection<AssessmentFlag> assessmentFlags, Long id) {
		if (assessmentFlags == null || id == null) {
			return null;
		}
		for (AssessmentFlag a : assessmentFlags) {
			if (id.equals(a.getId())) {
				return a;
			}
		}
		return null;
	}

	/**
	 * U kolekciji vrijednosti zastavica pronalazi onu koja ima zadani id zastavice.
	 * @param assessmentFlagValues kolekcija rezultata provjera
	 * @param id id zastavice koji se trazi
	 * @return vrijednost zastavice sa zadanim id-om ili null ako takva ne postoji
	 */
	public static AssessmentFlagValue getAssessmentFlagValueWithID(Collection<AssessmentFlagValue> assessmentFlagValues, Long id) {
		if (assessmentFlagValues == null || id == null) {
			return null;
		}
		for (AssessmentFlagValue a : assessmentFlagValues) {
			if (id.equals(a.getAssessmentFlag().getId())) {
				return a;
			}
		}
		return null;
	}

	public static Map<String, AssessmentAssistantSchedule> mapAssistantScheduleByJmbag(
			Collection<AssessmentAssistantSchedule> assistantSchedule) {
		Map<String, AssessmentAssistantSchedule> m = new HashMap<String, AssessmentAssistantSchedule>(
				assistantSchedule.size());
		for (AssessmentAssistantSchedule a : assistantSchedule) {
			m.put(a.getUser().getJmbag(), a);
		}
		return m;
	}

	public static Map<Long, AssessmentAssistantSchedule> mapAssistantScheduleByUserID(
			Set<AssessmentAssistantSchedule> assistantSchedule) {
		Map<Long, AssessmentAssistantSchedule> m = new HashMap<Long, AssessmentAssistantSchedule>(
				assistantSchedule.size());
		for (AssessmentAssistantSchedule a : assistantSchedule) {
			m.put(a.getUser().getId(), a);
		}
		return m;
	}
	
	public static Map<Long, AssessmentScore> mapAssessmentScoreByUserID(
			Collection<AssessmentScore> scores) {
		Map<Long, AssessmentScore> m = new HashMap<Long, AssessmentScore>(scores.size());
		for (AssessmentScore as : scores) {
			m.put(as.getUser().getId(), as);
		}
		return m;
	}
	public static Map<Long, AssessmentFlagValue> mapAssessmentFlagValueByUserID(
			Collection<AssessmentFlagValue> flags) {
		Map<Long, AssessmentFlagValue> m = new HashMap<Long, AssessmentFlagValue>(flags.size());
		for (AssessmentFlagValue afv : flags) {
			m.put(afv.getUser().getId(), afv);
		}
		return m;
	}
	public static Map<Long, AssessmentFlagValue> mapAssessmentFlagValueByID(
			Collection<AssessmentFlagValue> flags) {
		Map<Long, AssessmentFlagValue> m = new HashMap<Long, AssessmentFlagValue>(flags.size());
		for (AssessmentFlagValue afv : flags) {
			m.put(afv.getId(), afv);
		}
		return m;
	}

	/**
	 * Gradi set kratkih imena provjera koje se koriste na kolegijima. Ulaz je niz 
	 * parova (isvuSifra,kratkoImeProvjere).
	 * 
	 * @param allAssessmentNames parovi
	 * @return mapa
	 */
	public static Map<String, Set<String>> mapAssessmentShortNamesByISVUCode(List<Object[]> allAssessmentNames) {
		Map<String,Set<String>> res = new HashMap<String, Set<String>>(200);
		for(Object[] o : allAssessmentNames) {
			String isvu = (String)o[0];
			String shortName = (String)o[1];
			Set<String> set = res.get(isvu);
			if(set==null) {
				set = new HashSet<String>();
				res.put(isvu, set);
			}
			set.add(shortName);
		}
		return res;
	}

}
