package hr.fer.zemris.jcms.service.util;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.security.GroupSupportedPermission.MarketPlacePlacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupUtil {

	/**
	 * Utvrđuje koji je placement navedene grupe i burze.
	 * 
	 * @param g grupa
	 * @return položaj obzirom na burzu
	 */
	public static GroupSupportedPermission.MarketPlacePlacement getCourseGroupMarketPlacePlacement(Group g) {
		String rp = g.getRelativePath();
		if(rp.isEmpty()) return GroupSupportedPermission.MarketPlacePlacement.BEFORE_MARKET_PLACE;
		char[] chars = rp.toCharArray();
		int broj = 0;
		for(int i = 0; i < chars.length; i++) {
			if(chars[i]=='/') broj++;
		}
		// Ako nema slash-ova, to su top-level grupe kolegija
		if(broj==0) {
			if(rp.equals("0")) return GroupSupportedPermission.MarketPlacePlacement.IS_MARKET_PLACE;
			return GroupSupportedPermission.MarketPlacePlacement.BEFORE_MARKET_PLACE;
		}
		if(broj==1) {
			if(rp.startsWith("0/")) return GroupSupportedPermission.MarketPlacePlacement.AFTER_MARKET_PLACE;
			if(rp.startsWith("3/")) return GroupSupportedPermission.MarketPlacePlacement.BEFORE_MARKET_PLACE;
			return GroupSupportedPermission.MarketPlacePlacement.IS_MARKET_PLACE;
		}
		return GroupSupportedPermission.MarketPlacePlacement.AFTER_MARKET_PLACE;
	}
	
	/**
	 * Utvrđuje u kakvom položaju mora biti grupa dijete od predanog roditelja. Kako ovo nije uvijek
	 * moguće jednoznačno odrediti, metoda može vratiti <code>null</code> kada odluka nije jednoznačna. 
	 * @param parent roditelj
	 * @return položaj djeteta obzirom na burzu
	 */
	public static GroupSupportedPermission.MarketPlacePlacement getCourseGroupChildMarketPlacePlacement(Group parent) {
		String rp = parent.getRelativePath();
		// Ako sam ja primarna grupa kolegija, nemam odluku za djecu jer neka jesu burze, neka nisu
		if(rp.isEmpty()) return null;
		char[] chars = rp.toCharArray();
		int broj = 0;
		for(int i = 0; i < chars.length; i++) {
			if(chars[i]=='/') broj++;
		}
		// Ako nema slash-ova, to su top-level grupe kolegija
		if(broj==0) {
			if(rp.equals("0")) return GroupSupportedPermission.MarketPlacePlacement.AFTER_MARKET_PLACE;
			if(rp.equals("3")) return GroupSupportedPermission.MarketPlacePlacement.BEFORE_MARKET_PLACE;
			return GroupSupportedPermission.MarketPlacePlacement.IS_MARKET_PLACE;
		}
		if(broj==1) {
			if(rp.startsWith("3/")) return GroupSupportedPermission.MarketPlacePlacement.BEFORE_MARKET_PLACE;
			return GroupSupportedPermission.MarketPlacePlacement.AFTER_MARKET_PLACE;
		}
		return GroupSupportedPermission.MarketPlacePlacement.AFTER_MARKET_PLACE;
	}

	/**
	 * Treba li odmah definirati da je korisnik i vlasnik grupe?
	 * 
	 * @param g grupa
	 * @return <code>true</code> ako treba, <code>false</code> inače
	 */
	public static boolean shouldCreateCourseGroupOwnership(Group g) {
		// Za sada vlasništvo automatski dodajemo nad privatnim grupama
		if(g.getRelativePath().startsWith("6/")) return true;
		// Za sve ostalo to ne radimo
		return false;
	}
	
	/**
	 * Treba li odmah definirati da je korisnik i vlasnik djece grupe roditelja koji je predan?
	 * 
	 * @param g grupa
	 * @return <code>true</code> ako treba, <code>false</code> inače
	 */
	public static boolean shouldCreateCourseGroupChildOwnership(Group parent) {
		// Za sada vlasništvo automatski dodajemo nad privatnim grupama
		if(parent.getRelativePath().equals("6") || parent.getRelativePath().startsWith("6/")) return true;
		// Za sve ostalo to ne radimo
		return false;
	}
	
	public static Map<Long,UserGroup> mapUserGroupByUserID(Collection<UserGroup> userGroups) {
		Map<Long, UserGroup> map = new HashMap<Long, UserGroup>(userGroups.size());
		for(UserGroup ug : userGroups) {
			map.put(ug.getUser().getId(), ug);
		}
		return map;
	}

	public static Map<Long,Group> mapGroupByID(Collection<Group> groups) {
		Map<Long, Group> map = new HashMap<Long, Group>(groups.size());
		for(Group g : groups) {
			map.put(g.getId(), g);
		}
		return map;
	}

	public static Map<String,Group> mapGroupByRelativePath(Collection<Group> groups) {
		Map<String, Group> map = new HashMap<String, Group>(groups.size());
		for(Group g : groups) {
			map.put(g.getRelativePath(), g);
		}
		return map;
	}

	public static Map<String,Group> mapGroupByName(Collection<Group> groups) {
		Map<String, Group> map = new HashMap<String, Group>(groups.size());
		for(Group g : groups) {
			map.put(g.getName(), g);
		}
		return map;
	}

	public static Map<String, List<Group>> mapGroupByCompositeCourseID(List<Group> allGroups) {
		Map<String, List<Group>> map = new HashMap<String, List<Group>>(100);
		for(Group g : allGroups) {
			List<Group> l = map.get(g.getCompositeCourseID());
			if(l==null) {
				l = new ArrayList<Group>();
				map.put(g.getCompositeCourseID(), l);
			}
			l.add(g);
		}
		return map;
	}

	/**
	 * Prima kolekciju grupa. Pretpostavka metode je da je u toj  kolekciji za svaki compositeCourseID
	 * tocno jedna grupa koja je burza. U kolekciji mogu biti i grupe koje nisu burze (dakle, djeca burze)
	 * i te se grupe ignoriraju.
	 * @param allGroups
	 * @return
	 */
	public static Map<String, Group> mapMarketPlacesByCompositeCourseID(List<Group> allGroups) {
		Map<String, Group> map = new HashMap<String, Group>(100);
		for(Group g : allGroups) {
			if(!g.isManagedRoot()) continue;
			map.put(g.getCompositeCourseID(), g);
		}
		return map;
	}
	
	public static String findNextRelativePath(Group parent) {
		List<Integer> list = new ArrayList<Integer>();
		for(Group g : parent.getSubgroups()) {
			String relPath = g.getRelativePath();
			int p = relPath.lastIndexOf('/');
			Integer broj = Integer.valueOf(relPath.substring(p+1));
			list.add(broj);
		}
		Collections.sort(list);
		int prvi = list.indexOf(Integer.valueOf(1));
		if(prvi==-1) return parent.getRelativePath()+"/1";
		int ocekivao = 2;
		for(int i = prvi+1; i<list.size(); i++) {
			int nasao = list.get(i);
			if(nasao>ocekivao) break;
			ocekivao++;
		}
		return parent.getRelativePath()+"/"+ocekivao;
	}

	/**
	 * Na kolegiju pronalazi vršnu privatnu grupu.
	 * 
	 * @param courseInstance kolegij
	 * @return vršna privatna grupa, ili <code>null</code> ako ona ne postoji.
	 */
	public static Group findPrivateGroup(CourseInstance courseInstance) {
		Group privateGroup = null;
		for(Group group : courseInstance.getPrimaryGroup().getSubgroups()) {
			if("6".equals(group.getRelativePath())) {
				privateGroup = group;
				break;
			}
		}
		return privateGroup;
	}

	/**
	 * Metoda pronalazi burzu koja upravlja navedenom grupom, ako takva postoji. Ako ne, vraća <code>null</code>.
	 * Napomena: moguća je situacija da je grupa takva da predstavlja burzu, ali burza fizički još nije stvorena.
	 * U tom slučaju metoda će vratiti <code>null</code>, što ne znači da se promatrana grupa ne nalazi pod kontrolom
	 * neke burze.
	 *  
	 * @param g grupa čija se burza traži
	 * @return burzu ili <code>null</code> ako burze nema
	 */
	public static MarketPlace findMarketPlace(Group g) {
		while(g!=null) {
			if(g.isManagedRoot()) return g.getMarketPlace();
			g = g.getParent();
		}
		return null;
	}

	/**
	 * Metoda pronalazi grupu koja ima burzu koja upravlja navedenom grupom, ako takva postoji. Ako ne, vraća <code>null</code>.
	 * 
	 * @param g grupa čija se burza traži
	 * @return grupa s burzom ili <code>null</code> ako burze nema
	 */
	public static Group findMarketPlaceGroup(Group g) {
		while(g!=null) {
			if(g.isManagedRoot()) return g;
			g = g.getParent();
		}
		return null;
	}
	
	/**
	 * Metoda dohvaća listu grupa počev od predanog roditelja (koji se isto može naći u listi) za
	 * koje korisnik ima pravo pregledavanja članova grupe.
	 * 
	 * @param courseInstance kolegij
	 * @param group početna grupa
	 * @param onlyManagedGroups filtriraj samo one grupe koje su pod upravljanjem burze
	 * @return lista grupa, moguće prazna
	 */
	public static List<Group> retrieveUsersViewableGroups(CourseInstance courseInstance, Group group, boolean onlyManagedGroups) {
		List<Group> allGroups = new ArrayList<Group>();
		retrieveUsersViewableGroupsRecursive(courseInstance, group, allGroups, onlyManagedGroups);
		return allGroups;
	}

	private static void retrieveUsersViewableGroupsRecursive(CourseInstance courseInstance, Group group, List<Group> allGroups, boolean onlyManagedGroups) {
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(courseInstance, group);
		if(!gPerm.getCanView()) {
			return;
		}
		if(gPerm.getCanViewUsers()) {
			if(!onlyManagedGroups || gPerm.getPlacement()==MarketPlacePlacement.AFTER_MARKET_PLACE) allGroups.add(group);
		}
		for(Group child : group.getSubgroups()) {
			retrieveUsersViewableGroupsRecursive(courseInstance, child, allGroups, onlyManagedGroups);
		}
	}
	
	/**
	 * Metoda dohvaća listu grupa počev od predanog roditelja (koji se isto može naći u listi) za
	 * koje korisnik ima pravo uređivanja članova grupe.
	 * 
	 * @param courseInstance kolegij
	 * @param group početna grupa
	 * @param onlyManagedGroups filtriraj samo one grupe koje su pod upravljanjem burze
	 * @return lista grupa, moguće prazna
	 */
	public static List<Group> retrieveUsersManageableGroups(CourseInstance courseInstance, Group group, boolean onlyManagedGroups) {
		List<Group> allGroups = new ArrayList<Group>();
		retrieveUsersManageableGroupsRecursive(courseInstance, group, allGroups, onlyManagedGroups);
		return allGroups;
	}

	private static void retrieveUsersManageableGroupsRecursive(CourseInstance courseInstance, Group group, List<Group> allGroups, boolean onlyManagedGroups) {
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(courseInstance, group);
		if(!gPerm.getCanView()) {
			return;
		}
		if(gPerm.getCanManageUsers()) {
			if(!onlyManagedGroups || gPerm.getPlacement()==MarketPlacePlacement.AFTER_MARKET_PLACE) allGroups.add(group);
		}
		for(Group child : group.getSubgroups()) {
			retrieveUsersManageableGroupsRecursive(courseInstance, child, allGroups, onlyManagedGroups);
		}
	}
	
	/**
	 * Metoda dohvaća sve korisnike iz grupe i svih podgrupa nad kojima trenutni korisnik ima pravo mijenjanja podataka.
	 * 
	 * @param courseInstance kolegij
	 * @param group grupa
	 * @param initialCapacity inicijalni kapacitet liste; ako je manji od 16, kreće se sa 16
	 * @return listu korisnika
	 */
	public static List<UserGroup> retrieveAllManagedUsers(CourseInstance courseInstance, Group group, int initialCapacity) {
		if(initialCapacity < 16) initialCapacity = 16;
		List<UserGroup> list = new ArrayList<UserGroup>(initialCapacity);
		retrieveAllManagedUsersRecursive(courseInstance, group, list);
		return list;
	}

	private static void retrieveAllManagedUsersRecursive(CourseInstance courseInstance, Group group, List<UserGroup> list) {
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(courseInstance, group);
		if(!gPerm.getCanView()) {
			return;
		}
		if(gPerm.getCanManageUsers()) {
			list.addAll(group.getUsers());
		}
		for(Group child : group.getSubgroups()) {
			retrieveAllManagedUsersRecursive(courseInstance, child, list);
		}
	}
	
	/**
	 * Metoda dohvaća listu grupa kojima trenutni korisnik smije uređivati ownere.
	 * 
	 * @param courseInstance kolegij
	 * @param group početna grupa
	 * @return lista grupa, moguće prazna
	 */
	public static List<Group> retrieveManageableGroupOwners(CourseInstance courseInstance, Group group) {
		List<Group> allGroups = new ArrayList<Group>();
		retrieveManageableGroupOwnersRecursive(courseInstance, group, allGroups);
		return allGroups;
	}

	private static void retrieveManageableGroupOwnersRecursive(CourseInstance courseInstance, Group group, List<Group> allGroups) {
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(courseInstance, group);
		if(!gPerm.getCanView()) {
			return;
		}
		if(gPerm.getCanManageGroupOwners()) {
			allGroups.add(group);
		}
		for(Group child : group.getSubgroups()) {
			retrieveManageableGroupOwnersRecursive(courseInstance, child, allGroups);
		}
	}

}
