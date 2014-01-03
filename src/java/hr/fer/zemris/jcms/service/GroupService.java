package hr.fer.zemris.jcms.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.web.actions.data.GroupEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

@Deprecated
public class GroupService {

	@Deprecated
	public static void getNewGroupInputData(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long parentGroupID, final GroupBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(parentGroupID==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group parent = dh.getGroupDAO().get(em, parentGroupID);
				if(parent==null || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}

	@Deprecated
	public static void getNewGroupCreateData(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long parentGroupID, final GroupBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(parentGroupID==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group parent = dh.getGroupDAO().get(em, parentGroupID);
				if(parent==null || !parent.isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(StringUtil.isStringBlank(bean.getName())) {
					data.getMessageLogger().addErrorMessage("Niste zadali ime grupe!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				boolean found = false;
				for(Group g : parent.getSubgroups()) {
					if(g.getName().equals(bean.getName())) {
						found = true;
					}
				}
				if(found) {
					data.getMessageLogger().addErrorMessage("Grupa sa zadanim imenom već postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}

				String nextRelativePath = GroupUtil.findNextRelativePath(parent);
				Group g = new Group();
				g.setCapacity(-1);
				g.setCompositeCourseID(parent.getCompositeCourseID());
				g.setEnteringAllowed(false);
				g.setLeavingAllowed(false);
				g.setManagedRoot(false);
				g.setName(bean.getName());
				g.setRelativePath(nextRelativePath);
				g.setParent(parent);
				dh.getGroupDAO().save(em, g);
				parent.getSubgroups().add(g);
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyInserted"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void getGroupEditData(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long groupID, final GroupBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(groupID==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group group = dh.getGroupDAO().get(em, groupID);
				if(group==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				bean.setName(group.getName());
				bean.setId(group.getId());
				data.setGroup(group);
				data.setResult(AbstractActionData.RESULT_INPUT);
				return null;
			}
		});
	}

	@Deprecated
	public static void getGroupSaveData(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long parentGroupID, final GroupBean bean) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(bean.getId()==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group group = dh.getGroupDAO().get(em, bean.getId());
				if(group==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(StringUtil.isStringBlank(bean.getName())) {
					data.getMessageLogger().addErrorMessage("Niste zadali ime grupe!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				boolean found = false;
				for(Group g : group.getParent().getSubgroups()) {
					if(g.getId().equals(group.getId())) continue;
					if(g.getName().equals(bean.getName())) {
						found = true;
					}
				}
				if(found) {
					data.getMessageLogger().addErrorMessage("Grupa sa zadanim imenom već postoji!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				group.setName(bean.getName());
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void transferUsersFromGroup(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long sourceGroupID, final Long destinationGroupID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(sourceGroupID==null || destinationGroupID==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group sGroup = dh.getGroupDAO().get(em, sourceGroupID);
				Group dGroup = dh.getGroupDAO().get(em, destinationGroupID);
				if(sGroup==null || dGroup==null || !sGroup.getParent().getId().equals(dGroup.getParent().getId()) || !sGroup.getParent().isManagedRoot()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setGroup(dGroup);
				if(sGroup.getParent().getMarketPlace().getOpen()) {
					data.getMessageLogger().addErrorMessage("Burza za dotičnu grupu je otvorena. Molim zatvorite najprije burzu.");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}
				Set<Long> existingUsers = new HashSet<Long>(dGroup.getUsers().size());
				for(UserGroup ug : dGroup.getUsers()) {
					existingUsers.add(ug.getUser().getId());
				}
				List<UserGroup> members = new ArrayList<UserGroup>(sGroup.getUsers());
				sGroup.getUsers().clear();
				em.flush();
				for(UserGroup ug : members) {
					// Za ovog korisnika obriši stanje na burzi!
					dh.getMarketPlaceDAO().clearAllOffersForUser(em, sGroup.getParent().getMarketPlace(), ug.getUser(), ug.getGroup());
					if(existingUsers.contains(ug.getUser().getId())) {
						// Taj je vec unutra! Ovo izbrisi!
						em.remove(ug);
					} else {
						ug.setGroup(dGroup);
						dGroup.getUsers().add(ug);
					}
				}
				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	@Deprecated
	public static void deleteGroup(final GroupEditData data, final Long userID,
			final String courseInstanceID, final Long groupID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				if(!BasicBrowsing.fillCurrentUser(em, data, userID, "Error.noPermission", AbstractActionData.RESULT_FATAL)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(groupID==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Group group = dh.getGroupDAO().get(em, groupID);
				if(group==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!group.getParent().isManagedRoot()) {
					data.getMessageLogger().addErrorMessage("Ne možete brisati nadgrupe.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!group.getUsers().isEmpty()) {
					data.getMessageLogger().addErrorMessage("Grupa nije prazna. Brisanje nepraznih grupa nije dozvoljeno.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(!group.getSubgroups().isEmpty()) {
					data.getMessageLogger().addErrorMessage("Grupa ima podgrupe. Brisanje nepraznih grupa nije dozvoljeno.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(group.getParent().getMarketPlace().getOpen()) {
					data.getMessageLogger().addErrorMessage("Burza za dotičnu grupu je otvorena. Molim zatvorite najprije burzu.");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return null;
				}

				// Obrisi sve ponude grupe
				dh.getMarketPlaceDAO().clearAllOffersInvolvingGroup(em, group.getParent().getMarketPlace(), group);
				
				// Obrisi sve vlasnike grupe
				List<GroupOwner> gowners = dh.getGroupDAO().findForGroup(em, group);
				for(GroupOwner go : gowners) {
					em.remove(go);
				}
				if(!gowners.isEmpty()) {
					em.flush();
				}

				// Obrisi sve dogadaje grupe
				List<GroupWideEvent> allEvents = new ArrayList<GroupWideEvent>(group.getEvents());
				group.getEvents().clear();
				for(GroupWideEvent gwe : allEvents) {
					gwe.getGroups().remove(group);
				}
				if(!allEvents.isEmpty()) {
					em.flush();
					for(GroupWideEvent gwe : allEvents) {
						if(gwe.getGroups().isEmpty()) {
							// Mozemo obrisati i event
							em.remove(gwe);
						}
					}
					em.flush();
				}
				
				group.getParent().getSubgroups().remove(group);
				em.remove(group);
				em.flush();

				data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

}
