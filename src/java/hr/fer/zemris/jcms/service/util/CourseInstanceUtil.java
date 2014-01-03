package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.CourseInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Pomocni razred za manipuliranje kolekcijama objekata tipa CourseInstance.
 * 
 * @author marcupic
 *
 */
public class CourseInstanceUtil {

	/**
	 * Stvara mapu kolegija po ISVU sifri.
	 * 
	 * @param collection kolekcija kolegija
	 * @return mapa isvu sifra - kolegij
	 */
	public static Map<String, CourseInstance> mapCourseInstanceByISVUCode(Collection<CourseInstance> collection) {
		Map<String, CourseInstance> m = new HashMap<String, CourseInstance>(collection.size());
		for(CourseInstance ci : collection) {
			m.put(ci.getCourse().getIsvuCode(), ci);
		}
		return m;
	}
	
	/**
	 * Stvara mapu primjeraka kolegija po identifikatoru.
	 * 
	 * @param collection kolekcija kolegija
	 * @return mapa id - primjerak kolegija
	 */
	public static Map<String, CourseInstance> mapCourseInstanceByID(Collection<CourseInstance> collection) {
		Map<String, CourseInstance> m = new HashMap<String, CourseInstance>(collection.size());
		for(CourseInstance ci : collection) {
			m.put(ci.getId(), ci);
		}
		return m;
	}
}
