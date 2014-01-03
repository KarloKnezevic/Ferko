package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.AssessmentFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pomocni razred za manipuliranje kolekcijama objekata tipa {@link AssessmentFile}.
 * 
 * @author marcupic
 *
 */
public class AssessmentFileUtil {

	/**
	 * Stvara mapu datoteka po JMBAG-u korisnika.
	 * @param assessments
	 * @return
	 */
	public static Map<String, List<AssessmentFile>> mapByJMBAG(Collection<AssessmentFile> assessmentFiles) {
		Map<String, List<AssessmentFile>> m = new HashMap<String, List<AssessmentFile>>(assessmentFiles.size());
		for(AssessmentFile a : assessmentFiles) {
			String key = a.getUser() == null ? null : a.getUser().getJmbag();
			List<AssessmentFile> list = m.get(key);
			if(list==null) {
				list = new ArrayList<AssessmentFile>();
				m.put(key, list);
			}
			list.add(a);
		}
		return m;
	}

	public static AssessmentFile findFor(Map<String, List<AssessmentFile>> map, String jmbag, String descriptor) {
		List<AssessmentFile> list = map.get(jmbag);
		if(list==null) return null;
		for(int i = 0; i < list.size(); i++) {
			AssessmentFile a = list.get(i);
			if(descriptor.equals(a.getDescriptor())) return a;
		}
		return null;
	}
	
	public static void add(Map<String, List<AssessmentFile>> map, AssessmentFile a) {
		String key = a.getUser() == null ? null : a.getUser().getJmbag();
		List<AssessmentFile> list = map.get(key);
		if(list==null) {
			list = new ArrayList<AssessmentFile>();
			map.put(key, list);
		}
		list.add(a);
	}
}
