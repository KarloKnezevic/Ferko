package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Pomocni razred za manipuliranje kolekcijama objekata tipa User.
 * 
 * @author marcupic
 *
 */
public class UserUtil {

	/**
	 * Stvara mapu korisnika po jmbag-u.
	 * 
	 * @param collection kolekcija korisnika
	 * @return mapa jmbag - korisnik
	 */
	public static Map<String, User> mapUserByJmbag(Collection<User> collection) {
		Map<String, User> m = new HashMap<String, User>(collection.size());
		for(User u : collection) {
			m.put(u.getJmbag(), u);
		}
		return m;
	}
	
	/**
	 * Stvara mapu UserGroup razreda po jmbag-u.
	 * 
	 * @param collection
	 * @return
	 */
	public static Map<String, UserGroup> mapUserGroupByJmbag(Collection<UserGroup> collection) {
		Map<String, UserGroup> m = new HashMap<String, UserGroup>(collection.size());
		for (UserGroup ug : collection) {
			m.put(ug.getUser().getJmbag(), ug);
		}
		return m;
	}
	
	/**
	 * Stvaram mapu Usera po id-u
	 * 
	 * @param collection
	 * @return
	 */
	public static Map<Long, User> mapUserById(Collection<User> collection) {
		Map<Long, User> m = new HashMap<Long, User>(collection.size());
		for (User u : collection) {
			m.put(u.getId(), u);
		}
		return m;
	}
}
