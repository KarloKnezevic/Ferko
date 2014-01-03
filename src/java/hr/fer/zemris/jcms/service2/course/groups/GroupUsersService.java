package hr.fer.zemris.jcms.service2.course.groups;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.JCMSLogger;
import hr.fer.zemris.jcms.beans.UGPBean;
import hr.fer.zemris.jcms.beans.ext.StudentGroupTagBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.parsers.StudentGroupTagParser;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.security.GroupSupportedPermission.MarketPlacePlacement;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.ChangeUsersGroupData;
import hr.fer.zemris.jcms.web.actions.data.ShowGroupUsersData;
import hr.fer.zemris.jcms.web.actions.data.TransferUsersFromGroupData;
import hr.fer.zemris.jcms.web.actions.data.UpdateUserGroupMembershipData;
import hr.fer.zemris.jcms.web.actions.data.UploadStudentTagsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class GroupUsersService {

	public static void fetchGroupListing(EntityManager em, ShowGroupUsersData data) {

		// Popuni pocetne podatke
		if(data.getGroupID()!=null) {
			if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		} else {
			if(!GroupServiceSupport.loadGroup(em, data, data.getCourseInstanceID(), data.getRelativePath())) return;
		}
		
		// Provjeri dozvole
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(!gPerm.getCanView()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setGperm(gPerm);
		
		// Dohvati sve grupe cije studente smijem vidjeti
		List<Group> allGroups = GroupUtil.retrieveUsersViewableGroups(data.getCourseInstance(), data.getGroup(), false);
		Collections.sort(allGroups, StringUtil.GROUP_COMPARATOR);

		List<Group> managedGroups = null;
		Group mpg = GroupUtil.findMarketPlaceGroup(data.getGroup());
		// Ako:
		// a) grupa nema svog marketplace-a, ili
		// b) to nije list
		// tada ne nudimo mogućnost prebacivanja studenata iz drugih grupa u ovu (jer "ova" više nije jednoznačna)
		if(mpg==null || gPerm.getPlacement()!=MarketPlacePlacement.AFTER_MARKET_PLACE || !data.getGroup().getSubgroups().isEmpty()) {
			data.setTransferEnabled(false);
			data.setTransferGroups(new ArrayList<Group>());
		} else {
			data.setTransferEnabled(true);
			List<Group> transferGroups = GroupUtil.retrieveUsersManageableGroups(data.getCourseInstance(), data.getGroup().getParent(), true);
			transferGroups.remove(data.getGroup());
			Collections.sort(transferGroups, StringUtil.GROUP_COMPARATOR);
			data.setTransferGroups(transferGroups);
		}
		
		managedGroups = GroupUtil.retrieveUsersManageableGroups(data.getCourseInstance(), data.getGroup(), true);
		Collections.sort(managedGroups, StringUtil.GROUP_COMPARATOR);
		data.setManagedGroups(managedGroups);
		data.setMarketPlaceGroup(mpg);
		
		// Idemo popuniti listu studenata
		List<UGPBean> users = new ArrayList<UGPBean>(500);
		
		for(Group g : allGroups) {
			GroupSupportedPermission gp = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), g);
			Group marketPlace = GroupUtil.findMarketPlaceGroup(g);
			for(UserGroup ug : g.getUsers()) {
				UGPBean bean = new UGPBean(
					ug.getId(),
					ug.getUser().getId(),
					ug.getUser().getJmbag(),
					ug.getUser().getFirstName(),
					ug.getUser().getLastName(),
					ug.getGroup().getId(),
					ug.getGroup().getName(),
					ug.getTag(),
					marketPlace==null ? null : marketPlace.getId(),
					gp
				);
				users.add(bean);
			}
		}
		
		Collections.sort(users, new Comparator<UGPBean>() {
			@Override
			public int compare(UGPBean o1, UGPBean o2) {
				int r = StringUtil.HR_COLLATOR.compare(o1.getLastName(), o2.getLastName());
				if(r!=0) return r;
				r = StringUtil.HR_COLLATOR.compare(o1.getFirstName(), o2.getFirstName());
				if(r!=0) return r;
				return StringUtil.HR_COLLATOR.compare(o1.getJmbag(), o2.getJmbag());
			}
		});
		
		data.setAllGroups(allGroups);
		data.setAllUsers(users);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void prepareGroupChange(EntityManager em, ChangeUsersGroupData data) {

		if(!prepareGroupChangeInternal(em, data)) return;
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void performGroupChange(EntityManager em, ChangeUsersGroupData data) {

		if(!prepareGroupChangeInternal(em, data)) return;

		// Akcija je morala biti zaključana, i sve mora odgovarati...
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+data.getMarketPlaceGroup().getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		// Dohvati i odredišnu grupu
		Group newGroup = dh.getGroupDAO().get(em, data.getToGroupID());
		
		// Ako grupe nemaju istog roditelja, ili nova nije u onima cije korisnike mogu mijenjati:
		if(newGroup==null || !data.getGroup().getParent().equals(newGroup.getParent()) || !data.getOfferedGroups().contains(newGroup)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		MarketPlace mp = data.getMarketPlaceGroup().getMarketPlace();
		if(mp!=null) {
			dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, data.getUserGroup().getUser(), data.getGroup());
			JCMSLogger.getLogger().mpLogMove(mp, data.getUserGroup().getUser(), data.getGroup(), newGroup, data.getCurrentUser());
			em.flush();
		}
		
		data.getGroup().getUsers().remove(data.getUserGroup());
		data.getUserGroup().setGroup(newGroup);
		newGroup.getUsers().add(data.getUserGroup());
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void performGroupTransfer(EntityManager em, TransferUsersFromGroupData data) {
		
		// Popuni pocetne podatke
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group sourceGroup = dh.getGroupDAO().get(em, data.getSourceGroupID());
		Group mpg = dh.getGroupDAO().get(em, data.getMpID());
		
		if(sourceGroup==null || mpg==null || !mpg.equals(GroupUtil.findMarketPlaceGroup(data.getGroup())) || !mpg.equals(GroupUtil.findMarketPlaceGroup(sourceGroup)) || data.getGroup().equals(sourceGroup)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+mpg.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		GroupSupportedPermission gpDest = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		GroupSupportedPermission gpSrc = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), sourceGroup);
		if(!gpDest.getCanManageUsers() || !gpSrc.getCanManageUsers() || gpSrc.getPlacement()!=MarketPlacePlacement.AFTER_MARKET_PLACE || gpDest.getPlacement()!=MarketPlacePlacement.AFTER_MARKET_PLACE) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		// Sada je sve spremno za zamjenu
		Set<Long> existingUsers = new HashSet<Long>(data.getGroup().getUsers().size());
		for(UserGroup ug : data.getGroup().getUsers()) {
			existingUsers.add(ug.getUser().getId());
		}
		List<UserGroup> members = new ArrayList<UserGroup>(sourceGroup.getUsers());
		sourceGroup.getUsers().clear();
		em.flush();
		for(UserGroup ug : members) {
			// Za ovog korisnika obriši stanje na burzi!
			dh.getMarketPlaceDAO().clearAllOffersForUser(em, sourceGroup.getParent().getMarketPlace(), ug.getUser(), ug.getGroup());
			if(existingUsers.contains(ug.getUser().getId())) {
				// Taj je vec unutra! Ovo izbrisi!
				data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Info.userAlreadyInGroup"));
				em.remove(ug);
			} else {
				ug.setGroup(data.getGroup());
				data.getGroup().getUsers().add(ug);
			}
		}
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void addUsersToGroup(EntityManager em, UpdateUserGroupMembershipData data) {
		
		// Popuni pocetne podatke
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group mpg = dh.getGroupDAO().get(em, data.getMpID());
		
		if(mpg==null || !mpg.equals(GroupUtil.findMarketPlaceGroup(data.getGroup()))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+mpg.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		GroupSupportedPermission gp = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(!gp.getCanManageUsers() || gp.getPlacement()!=MarketPlacePlacement.AFTER_MARKET_PLACE) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		// Sada je sve spremno za ažuriranje
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());
		Map<String,User> courseUserMap = UserUtil.mapUserByJmbag(courseUsers);
		
		List<String> newJmbags = null;
		try {
			newJmbags = TextService.readerToStringList(new StringReader(data.getText()==null ? "" : data.getText()));
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage("Pogreška kod čitanja liste JMBAG-ova. Ništa nije mijenjano.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Set<String> newJmbagsSet = new HashSet<String>(newJmbags);
		
		Group group = data.getGroup();
		
		List<UserGroup> forRemoval = new ArrayList<UserGroup>(group.getUsers().size());
		Set<String> presentJMBAGs = new HashSet<String>(group.getUsers().size());
		for(UserGroup ug : group.getUsers()) {
			if(newJmbagsSet.contains(ug.getUser().getJmbag())) {
				// ovog sam htio, i imam ga; vozi dalje!
				presentJMBAGs.add(ug.getUser().getJmbag());
				continue;
			}
			// novi popis ga ne sadrži; ako želim takve maknuti:
			if(data.isRemoveOther()) {
				forRemoval.add(ug);
				continue;
			}
			presentJMBAGs.add(ug.getUser().getJmbag());
		}
		
		Set<String> forAddition = new HashSet<String>(newJmbagsSet);
		forAddition.removeAll(presentJMBAGs);
		Set<User> forAdditionUsers = new HashSet<User>(forAddition.size());
		boolean error = false;
		for(String jmbag : forAddition) {
			User u = courseUserMap.get(jmbag);
			if(u!=null) {
				forAdditionUsers.add(u);
			} else {
				error = true;
				data.getMessageLogger().addErrorMessage("Student "+jmbag+" ne postoji na kolegiju!");
			}
		}
		if(error) {
			data.getMessageLogger().addErrorMessage("Zbog prethodnih pogrešaka ažuriranje je preskočeno i ništa nije mijenjano.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		// inace, sve je OK. Najprije dodaj one koje trebaš dodati:
		for(User user : forAdditionUsers) {
			UserGroup ug = new UserGroup();
			ug.setGroup(group);
			ug.setUser(user);
			dh.getUserGroupDAO().save(em, ug);
			group.getUsers().add(ug);
		}
		
		// Sada počisti one koje trebaš počistiti
		MarketPlace mp = mpg.getMarketPlace();
		for(UserGroup ug : forRemoval) {
			if(mp != null) {
				em.flush();
				dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, ug.getUser(), group);
			}
			group.getUsers().remove(ug);
			dh.getUserGroupDAO().remove(em, ug);
		}
		
		data.getMessageLogger().addErrorMessage("Dodano "+forAdditionUsers.size()+" korisnika, uklonjeno "+forRemoval.size()+" korisnika.");
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda ažurira tagove studenata u grupama.
	 * Nuspojava je brisanje ponuda na burzi ažuriranih studenata. Pojašnjenje je u starom komentaru:<br>
	 * <code>// pazi, ova akcije je opasna, jer ako su grupe na burzi, tagovi su preslikani u ponude a ja ih ovdje mjenjam izvana sto je lose! Rjesenje: nakon ovoga izbrisati sve ponude.</code>
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void updateGroupUserTags(EntityManager em, UploadStudentTagsData data) {
		
		// Popuni pocetne podatke
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group mpg = dh.getGroupDAO().get(em, data.getMpID());
		
		if(mpg==null || !mpg.equals(GroupUtil.findMarketPlaceGroup(data.getGroup()))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+mpg.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		GroupSupportedPermission gp = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(!gp.getCanManageUsers() || gp.getPlacement()!=MarketPlacePlacement.AFTER_MARKET_PLACE) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(data.getText()==null) data.setText("");
		List<StudentGroupTagBean> beans = null;
		try {
			beans = StudentGroupTagParser.parseTabbedFormat(new StringReader(data.getText()));
		} catch(IOException ex) {
			data.getMessageLogger().addErrorMessage("Dogodila se je pogreška prilikom čitanja ulaznih podataka: "+ex.getMessage());
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		// Sada je sve spremno za ažuriranje
		
		// Map<groupName,Map<jmbag,tag>>
		Map<String,Map<String,String>> usersByGroupsMap = new HashMap<String, Map<String,String>>(16);
		for(StudentGroupTagBean bean : beans) {
			Map<String,String> groupMap = usersByGroupsMap.get(bean.getGroupName());
			if(groupMap==null) {
				groupMap = new HashMap<String, String>(128);
				usersByGroupsMap.put(bean.getGroupName(), groupMap);
			}
			groupMap.put(bean.getJmbag(), bean.getTagName());
		}
		List<UserGroup> modifiableUsers = GroupUtil.retrieveAllManagedUsers(data.getCourseInstance(), data.getGroup(), 256);
		List<UserGroup> updatedUsers = new ArrayList<UserGroup>(modifiableUsers.size());
		int updatedCount = 0;
		for(UserGroup ug : modifiableUsers) {
			Map<String,String> groupMap = usersByGroupsMap.get(ug.getGroup().getName());
			if(groupMap!=null) {
				if(groupMap.containsKey(ug.getUser().getJmbag())) {
					String newTag = groupMap.get(ug.getUser().getJmbag());
					if(newTag==null && ug.getTag()==null) continue;
					if(newTag!=null && newTag.equals(ug.getTag())) continue;
					ug.setTag(newTag);
					updatedCount++;
					updatedUsers.add(ug);
					continue;
				}
			}
			groupMap = usersByGroupsMap.get("%");
			if(groupMap!=null) {
				if(groupMap.containsKey(ug.getUser().getJmbag())) {
					String newTag = groupMap.get(ug.getUser().getJmbag());
					if(newTag==null && ug.getTag()==null) continue;
					if(newTag!=null && newTag.equals(ug.getTag())) continue;
					ug.setTag(newTag);
					updatedCount++;
					updatedUsers.add(ug);
					continue;
				}
			}
		}
		
		// Sada jos za svakog studenta kojemu je promijenjen tag obriši ponude na burzi
		MarketPlace mp = mpg.getMarketPlace();
		if(mp != null) {
			for(UserGroup ug : updatedUsers) {
				dh.getMarketPlaceDAO().clearAllOffersForUser(em, mp, ug.getUser(), ug.getGroup());
			}
		}
		data.getMessageLogger().addErrorMessage("Ažurirano je "+updatedCount+" zapisa.");
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static boolean prepareGroupChangeInternal(EntityManager em, ChangeUsersGroupData data) {

		// Popuni pocetne podatke
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return false;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		data.setUserGroup(dh.getUserGroupDAO().get(em, data.getUgID()));

		if(data.getUserGroup()==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		
		if(!data.getUserGroup().getGroup().equals(data.getGroup())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleGroupData"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return false;
		}
		
		// Provjeri dozvole
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(!gPerm.getCanManageUsers()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		// Ovaj scenarij ne smije biti moguć!
		if(data.getGroup().getParent()==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		// Pronadi jos i grupu koja je marketplace za navedenu grupu i uvjeri se da ona postoji i da doista je marketplace
		Group gmp = dh.getGroupDAO().get(em, data.getMpID());
		if(gmp==null || !gmp.equals(GroupUtil.findMarketPlaceGroup(data.getGroup()))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setMarketPlaceGroup(gmp);

		// Inače idemo dohvatiti svu djecu roditelja
		List<Group> offeredGroups = new ArrayList<Group>(data.getGroup().getParent().getSubgroups());
		Iterator<Group> it = offeredGroups.iterator();
		while(it.hasNext()) {
			Group g = it.next();
			if(g.equals(data.getGroup())) {
				it.remove();
				continue;
			}
			GroupSupportedPermission gp = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), g);
			if(!gp.getCanManageUsers()) {
				it.remove();
				continue;
			}
		}
		Collections.sort(offeredGroups, StringUtil.GROUP_COMPARATOR);
		
		data.setOfferedGroups(offeredGroups);
		
		return true;
	}
	
	
}
