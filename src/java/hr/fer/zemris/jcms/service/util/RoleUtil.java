package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.Role;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoleUtil {

	public static Map<String, Role> mapRolesByName(Collection<Role> collection) {
		Map<String, Role> m = new HashMap<String, Role>(collection.size());
		for(Role r : collection) {
			m.put(r.getName(), r);
		}
		return m;
	}

}
