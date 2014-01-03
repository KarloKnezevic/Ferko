package hr.fer.zemris.jcms.service2.course.groups;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.security.GroupSupportedPermission.MarketPlacePlacement;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.GroupEditData;
import hr.fer.zemris.jcms.web.actions.data.ShowGroupTree2Data;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;
import hr.fer.zemris.util.Tree;
import hr.fer.zemris.util.TreeNode;

import javax.persistence.EntityManager;

public class GroupTreeBrowserService {

	/**
	 * Metoda dohvaća stablo grupa.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchGroupTree(EntityManager em, ShowGroupTree2Data data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Ovo je prije bilo korišeno, ali zapravo se čini da nije; eto neka stoji za sada tu.
		String relPath = "";
		
		// Provjeri dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canViewGroupTree(data.getCourseInstance(), relPath);
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ensurePrivateGroupExists(em, data);
		
		Tree<Group, GroupSupportedPermission> accessibleGroupsTree = JCMSSecurityManagerFactory.getManager().getAccessibleGroupTree(data.getCourseInstance());
		data.setAccessibleGroupsTree(accessibleGroupsTree);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda dohvaćeno stablo grupa serijalizira u JSON oblik i pohranjuje kao string.
	 * 
	 * @param data podatkovni objekt
	 * @return JSON format stabla
	 */
	public static String convertGroupTreeToJSON(ShowGroupTree2Data data) {
		StringWriter wr = new StringWriter(1024*128);
		JSONWriter js = new JSONWriter(wr);
		Tree<Group, GroupSupportedPermission> tree = data.getAccessibleGroupsTree();
		try {
			for(TreeNode<Group, GroupSupportedPermission> n : tree.getChildren()) {
				js.object();
				js.key("linkBuilder");
				js.object();
				js.key("l_1"); 
				js.object();
				js.key("purl"); js.value("ShowGroupUsers.action?groupID="); 
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.listGroupUsers")); 
				js.endObject();
				js.key("l_2");
				js.object();
				js.key("purl"); js.value("ExportGroupMembershipTree.action?groupID="); 
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.exportGroupMembershipTree")); 
				js.endObject();
				js.key("l_3");
				js.object();
				js.key("purl"); js.value("ExportGroupMembershipTree.action?format=mm&groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.exportGroupMembershipTreeMM")); 
				js.endObject();
				js.key("l_4");
				js.object();
				js.key("purl"); js.value("ListGroupEvents.action?groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.listGroupEvents"));
				js.endObject();
				js.key("l_5");
				js.object();
				js.key("purl"); js.value("ShowGroupTree!newSubgroupsPrepare.action?data.parentID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.addSubgroups"));
				js.endObject();
				js.key("l_6");
				js.object();
				js.key("purl"); js.value("GroupDelete.action?data.lid="+data.getCourseInstance().getId()+"&data.groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.deleteGroup"));
				js.endObject();
				js.key("l_7");
				js.object();
				js.key("purl"); js.value("GroupEdit!groupEdit.action?data.groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.editGroup"));
				js.endObject();
				js.key("l_8");
				js.object();
				js.key("purl"); js.value("ExportGroupMembershipTree.action?format=csv&groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.exportGroupMembershipTreeCSV")); 
				js.endObject();
				js.key("l_9");
				js.object();
				js.key("purl"); js.value("SubgroupOwners!show.action?groupID=");
				js.key("plabel"); js.value(data.getMessageLogger().getText("Navigation.groupOwners"));
				js.endObject();
				js.endObject();
				js.key("title").value(n.getElement().getName());
				js.key("e").value(n.getData().getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE ? 1 : 0);
				boolean dalje = !n.getChildren().isEmpty();
				js.key("o").value( !dalje ? "L" : "T");
				if(dalje) {
					js.key("i");
					convertGroupTreeToJSON(data, js, n.getChildren());
				}
				js.endObject();
			}
		} catch(Exception ex) {
			return "{}";
		}
		return wr.toString();
	}

	public static void newSubgroupsPrepare(EntityManager em, ShowGroupTree2Data data) {
		
		if(!loadParentGroup(em, data)) return;
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void newSubgroupsAdd(EntityManager em, ShowGroupTree2Data data) {
		
		if(!loadParentGroup(em, data)) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		List<String> names = new ArrayList<String>();
		
		if(StringUtil.isStringBlank(data.getText())) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.nothingToDo"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		String[] lines = data.getText().split("\r|\n");
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			names.add(line);
		}
		
		if(names.isEmpty()) {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("Warning.nothingToDo"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}

		if(names.size()>1 && !data.getAllowMultipleAddition()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.multipleGroupsNotAllowedHere"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		Group parent = data.getParent();
		GroupSupportedPermission.MarketPlacePlacement childPl = GroupUtil.getCourseGroupChildMarketPlacePlacement(parent);
		Map<String, Group> mapByName = GroupUtil.mapGroupByName(parent.getSubgroups());
		boolean errors = false;
		Set<String> zaDodati = new HashSet<String>();
		for(String name : names) {
			if(mapByName.containsKey(name)) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.groupAlreadyExists")+name);
				continue;
			}
			if(!zaDodati.add(name)) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.duplicateGroupFound")+name);
				continue;
			}
		}
		
		if(errors) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		boolean addOwner = GroupUtil.shouldCreateCourseGroupChildOwnership(parent);
		
		for(String name : names) {
			String nextRelativePath = GroupUtil.findNextRelativePath(parent);
			Group g = new Group();
			g.setCapacity(-1);
			g.setCompositeCourseID(parent.getCompositeCourseID());
			g.setEnteringAllowed(false);
			g.setLeavingAllowed(false);
			g.setManagedRoot(childPl==MarketPlacePlacement.IS_MARKET_PLACE);
			g.setName(name);
			g.setRelativePath(nextRelativePath);
			g.setParent(parent);
			if(g.isManagedRoot()) {
				MarketPlace mp = new MarketPlace();
				mp.setGroup(g);
				mp.setTimeBuffer(-1);
				g.setMarketPlace(mp);
			}
			dh.getGroupDAO().save(em, g);
			parent.getSubgroups().add(g);
			mapByName.put(name, g);
			if(addOwner) {
				GroupOwner go = new GroupOwner(g, data.getCurrentUser());
				dh.getGroupDAO().save(em, go);
			}
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.groupCreated")+name);
		}
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void editGroupPrepare(EntityManager em, GroupEditData data) {
		
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		// Provjeri dozvole
		GroupSupportedPermission p = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		boolean canEdit = p.getCanEdit(); 
		if(!canEdit) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setGroupName(data.getGroup().getName());
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void editGroupUpdate(EntityManager em, GroupEditData data) {
		
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		// Provjeri dozvole
		GroupSupportedPermission p = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		boolean canEdit = p.getCanEdit(); 
		if(!canEdit) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(StringUtil.isStringBlank(data.getGroupName())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nameMustBeGiven"));
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		if(data.getGroup().getName().equals(data.getGroupName())) {
			// Nemam sta za raditi...
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		// Ako postoji roditelj, ime grupe unutar njega mora ostati jedinstveno!
		if(data.getGroup().getParent()!=null) {
			Map<String,Group> mapByName = GroupUtil.mapGroupByName(data.getGroup().getParent().getSubgroups());
			if(mapByName.containsKey(data.getGroupName())) {
				// To ime je već unutra, a prethodno sam provjerio da nije sama grupa koja se ureduje; to je greska!
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.groupAlreadyExists")+data.getGroupName());
				data.setResult(AbstractActionData.RESULT_INPUT);
				return;
			}
		}
		
		// Ažurirajmo...
		data.getGroup().setName(data.getGroupName());
		
		// Gotovi smo
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja briše brupu i sve njezine podgrupe, korisnike, ownere, ponude na burzi itd, itd.
	 * Treba još riješiti zaključavanje!!!
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void deleteGroup(EntityManager em, GroupEditData data) {
		
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		// Provjeri dozvole
		GroupSupportedPermission p = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		boolean canDelete = p.getCanDelete();
		if(!canDelete) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(!"yes".equals(data.getConfirmed())) {
			data.setResult(AbstractActionData.RESULT_CONFIRM);
			return;
		}
		
		fullyDeleteGroups(em, data.getGroup().getId());
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}


	/**
	 * Pomoćna metoda koja implementira stvarno brisanje.
	 * 
	 * @param em entity manager
	 * @param groupID identifikator grupe koja se briše
	 */
	public static void fullyDeleteGroups(EntityManager em, Long groupID) {
		
		List<Group> toDelete = new ArrayList<Group>(512);

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		toDelete.add(dh.getGroupDAO().get(em, groupID));
		
		for(int i = 0; i < toDelete.size(); i++) {
			Group g = toDelete.get(i);
			for(Group c : g.getSubgroups()) {
				toDelete.add(c);
			}
		}
		
		// Krenimo sada od kraja:
		for(int i = toDelete.size()-1; i >= 0; i--) {
			Group g = toDelete.get(i);
			
			// Ako je pod marketplace-om, obrisi sve ponude:
			MarketPlace mp = GroupUtil.findMarketPlace(g);
			if(mp!=null) {
				// Obrisi sve ponude grupe
				dh.getMarketPlaceDAO().clearAllOffersInvolvingGroup(em, mp, g);
			}
			
			// Ako ima korisnika, obriši ih:
			if(!g.getUsers().isEmpty()) {
				for(UserGroup ug : g.getUsers()) {
					dh.getUserGroupDAO().remove(em, ug);
				}
				g.getUsers().clear();
				em.flush();
			}
			
			// Ako ima događaja, obriši ih:
			if(!g.getEvents().isEmpty()) {
				List<GroupWideEvent> eventsToRemove = new ArrayList<GroupWideEvent>();
				for(GroupWideEvent gwe : g.getEvents()) {
					gwe.getGroups().remove(g);
					// Ako je nakon ovoga event vise nema grupa, bio je samo moj, pa ga treba obrisati:
					if(gwe.getGroups().isEmpty()) {
						eventsToRemove.add(gwe);
					}
				}
				g.getEvents().clear();
				em.flush();
				for(GroupWideEvent gwe : eventsToRemove) {
					dh.getEventDAO().remove(em, gwe);
				}
			}
			
			// Obrisi sve vlasnike grupe
			List<GroupOwner> gowners = dh.getGroupDAO().findForGroup(em, g);
			if(!gowners.isEmpty()) {
				for(GroupOwner go : gowners) {
					em.remove(go);
				}
				em.flush();
			}

			// Ukloni grupu iz roditeljske kolekcije
			if(g.getParent()!=null) {
				g.getParent().getSubgroups().remove(g);
			}

			// Obriši grupu
			g.setParent(null);
			dh.getGroupDAO().remove(em, g);
		}
	}

	/**
	 * Metoda traži privatnu grupu na kolegiju, i ako je ne nađe, stvara je.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	private static void ensurePrivateGroupExists(EntityManager em, ShowGroupTree2Data data) {
		// Postoje li privatne grupe na kolegiju? Ako ne, dodaj!
		Group privateGroup = null;
		for(Group g : data.getCourseInstance().getPrimaryGroup().getSubgroups()) {
			if(g.getRelativePath().equals("6")) {
				privateGroup = g;
				break;
			}
		}
		if(privateGroup==null) {
			DAOHelper dh = DAOHelperFactory.getDAOHelper();
			privateGroup = new Group();
			privateGroup.setCompositeCourseID(data.getCourseInstance().getId());
			privateGroup.setManagedRoot(false);
			privateGroup.setName("Privatne grupe");
			privateGroup.setParent(data.getCourseInstance().getPrimaryGroup());
			privateGroup.setRelativePath("6");
			dh.getGroupDAO().save(em, privateGroup);
			data.getCourseInstance().getPrimaryGroup().getSubgroups().add(privateGroup);
		}
	}
	
	private static boolean loadParentGroup(EntityManager em, ShowGroupTree2Data data) {
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		// Dohvati grupu roditelja
		Group g = dh.getGroupDAO().get(em, data.getParentID());
		if(g==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setParent(g);
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, g.getCompositeCourseID())) return false;

		// Provjeri dozvole
		GroupSupportedPermission p = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), g);
		boolean canAdd = p.getCanAddSubgroups(); 
		if(!canAdd) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		ensurePrivateGroupExists(em, data);
		
		// Dodavanje više grupa od jednom dozvoljavamo samo ako su to djeca pod nadzorom burze; inače ne može!
		data.setAllowMultipleAddition(p.getPlacement()==MarketPlacePlacement.IS_MARKET_PLACE);

		return true;
	}

	private static void convertGroupTreeToJSON(ShowGroupTree2Data data, JSONWriter js, List<TreeNode<Group, GroupSupportedPermission>> children) throws JSONException {
		js.object();
		for(TreeNode<Group, GroupSupportedPermission> n : children) {
			js.key("groupID_"+n.getElement().getId());
			js.object();
			js.key("title").value(n.getElement().getName().isEmpty() ? "-" : n.getElement().getName());
			boolean dalje = !n.getChildren().isEmpty();
			js.key("o").value(!dalje ? "L" : "T");
			convertGroupTreeToJSON_AddMenus(data, js, n);
			if(n.getData().getCanViewUsers()) {
				
			}
			if(dalje) {
				js.key("e").value(n.getData().getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE ? 1 : 0);
				js.key("i");
				convertGroupTreeToJSON(data, js, n.getChildren());
			}
			js.endObject();
		}
		js.endObject();
	}

	private static void convertGroupTreeToJSON_AddMenus(ShowGroupTree2Data data, JSONWriter js, TreeNode<Group, GroupSupportedPermission> n) throws JSONException {
		boolean add = false;
		// Najprije provjere, da znam da li uopće započeti actions sekciju
		if(n.getData().getCanViewUsers()) { add = true; }
		if(n.getData().getCanViewEvents()) { add = true; }
		if(n.getData().getCanAddSubgroups()) { add = true; }
		if(n.getData().getCanDelete()) { add = true; }
		if(n.getData().getCanEdit()) { add = true; }
		if(!add) return;
		js.key("a");
		js.array();
		if(n.getData().getCanViewUsers()) {
			js.object();
			js.key("pu").value("1");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
			js.object();
			js.key("pu").value("8");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
			js.object();
			js.key("pu").value("2");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
			js.object();
			js.key("pu").value("3");
			js.key("pl").value(n.getElement().getId());
			js.endObject();

//			js.object();
//			js.key("url").value("ShowGroupUsers.action?courseInstanceID="+data.getCourseInstance().getId()+"&relativePath="+n.getElement().getRelativePath());
//			js.key("label").value(data.getMessageLogger().getText("Navigation.listGroupUsers"));
//			js.endObject();
//			js.object();
//			js.key("url").value("ExportGroupMembershipTree.action?courseInstanceID="+data.getCourseInstance().getId()+"&relativePath="+n.getElement().getRelativePath());
//			js.key("label").value(data.getMessageLogger().getText("Navigation.exportGroupMembershipTree"));
//			js.endObject();
//			js.object();
//			js.key("url").value("ExportGroupMembershipTree.action?courseInstanceID="+data.getCourseInstance().getId()+"&format=mm&&relativePath="+n.getElement().getRelativePath());
//			js.key("label").value(data.getMessageLogger().getText("Navigation.exportGroupMembershipTreeMM"));
//			js.endObject();

		}
		if(n.getData().getCanViewEvents()) {
			js.object();
			js.key("pu").value("4");
			js.key("pl").value(n.getElement().getId());
			js.endObject();

//			js.object();
//			js.key("url").value("ListGroupEvents.action?courseInstanceID="+data.getCourseInstance().getId()+"&relativePath="+n.getElement().getRelativePath());
//			js.key("label").value(data.getMessageLogger().getText("Navigation.listGroupEvents"));
//			js.endObject();
		}
		if(n.getData().getPlacement()!=MarketPlacePlacement.BEFORE_MARKET_PLACE) {
			js.object();
			js.key("pu").value("9");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
		}
		if(n.getData().getCanAddSubgroups()) {
			js.object();
			js.key("pu").value("5");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
		}
		if(n.getData().getCanDelete()) {
			js.object();
			js.key("pu").value("6");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
		}
		if(n.getData().getCanEdit()) {
			js.object();
			js.key("pu").value("7");
			js.key("pl").value(n.getElement().getId());
			js.endObject();
		}
		js.endArray();
	}

	/**
	 * Metoda na kolegiju pronalazi grupu kojom započinju privatne grupe.
	 * 
	 * @param courseInstance kolegij
	 * @return vršna privatna grupa
	 */
	static Group findPrivateGroup(CourseInstance courseInstance) {
		Group privateGroup = null;
		for(Group group : courseInstance.getPrimaryGroup().getSubgroups()) {
			if("6".equals(group.getRelativePath())) {
				privateGroup = group;
				break;
			}
		}
		return privateGroup;
	}

}
