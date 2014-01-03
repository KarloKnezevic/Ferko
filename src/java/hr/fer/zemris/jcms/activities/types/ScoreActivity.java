package hr.fer.zemris.jcms.activities.types;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import hr.fer.zemris.jcms.activities.AbstractCourseActivity;

public class ScoreActivity extends AbstractCourseActivity {

	private static final long serialVersionUID = 1L;
	
	/** 
	 * Koje sve komponente? Ovo je lista identifikatora oblika 
	 * aID#bodovi#shortName (za provjere) ili fID#vrijednost#shortName (za zastavice); 
	 * npr. a17#23.17#LAB1
	 **/
	private List<String> components = new LinkedList<String>();
	
	/**
	 * Konstruktor.
	 * 
	 * @param date datum
	 * @param courseInstanceID identifikator primjerka kolegija
	 * @param userID identifikator korisnika
	 * @param grade ocjena
	 * @param rang rang
	 * @param kind vrsta događaja
	 */
	public ScoreActivity(Date date, String courseInstanceID, Long userID,
			String initialValue) {
		super(userID, date, courseInstanceID);
		components.add(initialValue);
	}
	
	public List<String> getComponents() {
		return components;
	}
}
