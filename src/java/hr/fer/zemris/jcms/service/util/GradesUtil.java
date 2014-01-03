package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.Grade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GradesUtil {

	public static Map<Long, Grade> mapGradeByUserID(Collection<Grade> grades) {
		Map<Long, Grade> m = new HashMap<Long, Grade>(grades.size());
		for (Grade g : grades) {
			m.put(g.getUser().getId(), g);
		}
		return m;
	}

}
