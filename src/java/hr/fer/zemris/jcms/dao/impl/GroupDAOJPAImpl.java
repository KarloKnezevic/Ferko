package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.beans.ext.CoarseGroupStat2;
import hr.fer.zemris.jcms.dao.GroupDAO;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.List;

import javax.persistence.EntityManager;

public class GroupDAOJPAImpl implements GroupDAO {

	@Override
	public Group get(EntityManager em, Long id) {
		return em.find(Group.class, id);
	}

	@Override
	public void remove(EntityManager em, Group g) {
		em.remove(g);
	}

	@Override
	public void save(EntityManager em, Group g) {
		em.persist(g);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Group get(EntityManager em, String compositeCourseID,
			String relativePath) {
		List<Group> list = (List<Group>)em.createNamedQuery("Group.findGroup")
		.setParameter("compositeCourseID", compositeCourseID)
		.setParameter("relativePath", relativePath)
		.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findAllGroupsForUserOnCourse(EntityManager em,
			String compositeCourseID, User user) {
		return (List<Group>)em.createNamedQuery("Group.findForUser2")
			.setParameter("compositeCourseID", compositeCourseID)
			.setParameter("user", user)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findSubGroupsForUser(EntityManager em,
			String compositeCourseID, String parentRelativePath, User user) {
		return (List<Group>)em.createNamedQuery("Group.findForUser3")
			.setParameter("compositeCourseID", compositeCourseID)
			.setParameter("user", user)
			.setParameter("relativePath", parentRelativePath+"/%")
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Group findCourseInstancePrimaryGroup(EntityManager em, String compositeIdentifier) {
		List<Group> list = em.createNamedQuery("Group.findPrimary").setParameter("compositeCourseID", compositeIdentifier).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findCourseInstanceTopLevelGroups(
			EntityManager em, String compositeIdentifier) {
		List<Group> list = em.createNamedQuery("Group.findTopLevel").setParameter("compositeCourseID", compositeIdentifier).getResultList();
		return list;
	}
	
	@Override
	public List<Group> findLectureSubgroups(EntityManager em,
			String compositeIdentifier) {
		return findSubgroups(em, compositeIdentifier, "0/%");
	}
	
	@Override
	public Group findLectureSubgroupTree(EntityManager em,
			String compositeIdentifier) {
		List<Group> list = findSubgroups(em, compositeIdentifier, "0", "0/%");
		for(Group g : list) {
			if("0".equals(g.getRelativePath())) {
				return g;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findSubgroups(EntityManager em,
			String compositeIdentifier, String relativePath) {
		List<Group> list = em.createNamedQuery("Group.findSubgroups")
				.setParameter("compositeCourseID", compositeIdentifier)
				.setParameter("relativePath", relativePath)
				.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findSubgroups(EntityManager em,
			String compositeIdentifier, String eqRelativePath,
			String likeRelativePath) {
		List<Group> list = em.createNamedQuery("Group.findSubgroups2")
				.setParameter("compositeCourseID", compositeIdentifier)
				.setParameter("eqRelativePath", eqRelativePath)
				.setParameter("likeRelativePath", likeRelativePath)
				.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findSubgroupsLLE(EntityManager em,
			String likeCompositeIdentifier, String eqRelativePath,
			String likeRelativePath) {
		List<Group> list = em.createNamedQuery("Group.findSubgroups3")
				.setParameter("compositeCourseID", likeCompositeIdentifier)
				.setParameter("eqRelativePath", eqRelativePath)
				.setParameter("likeRelativePath", likeRelativePath)
				.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> findAllLectureUserGroups(EntityManager em,
			String likeCompositeIdentifier) {
		List<UserGroup> list = em.createNamedQuery("UserGroup.findAllLectureUGForSemester")
			.setParameter("compositeCourseID", likeCompositeIdentifier)
			.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupOwner> findForGroup(EntityManager em, Group group) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.findForGroup")
		.setParameter("group", group)
		.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupOwner> findForSubgroupsELE(EntityManager em,
			String eqCompositeIdentifier, String eqRelativePath,
			String likeRelativePath) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.findForSubgroupsELE")
		.setParameter("compositeCourseID", eqCompositeIdentifier)
		.setParameter("eqRelativePath", eqRelativePath)
		.setParameter("likeRelativePath", likeRelativePath)
		.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupOwner> findForSubgroupsLLE(EntityManager em,
			String likeCompositeIdentifier, String eqRelativePath,
			String likeRelativePath) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.findForSubgroupsLLE")
		.setParameter("compositeCourseID", likeCompositeIdentifier)
		.setParameter("eqRelativePath", eqRelativePath)
		.setParameter("likeRelativePath", likeRelativePath)
		.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupOwner> findForSubgroups(EntityManager em, String compositeIdentifier, String relativePath) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.findForSubgroups")
		.setParameter("compositeCourseID", compositeIdentifier)
		.setParameter("relativePath", relativePath+"/%")
		.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupOwner> findForSubgroupsAndUser(EntityManager em, String compositeIdentifier, String relativePath, User user) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.findForSubgroupsAndUser")
		.setParameter("compositeCourseID", compositeIdentifier)
		.setParameter("relativePath", relativePath+"/%")
		.setParameter("user", user)
		.getResultList();
		return list;
	}

	@Override
	public GroupOwner getGroupOwner(EntityManager em, Long id) {
		return em.find(GroupOwner.class, id);
	}

	@Override
	public void remove(EntityManager em, GroupOwner groupOwner) {
		em.remove(groupOwner);
	}

	@Override
	public void save(EntityManager em, GroupOwner groupOwner) {
		em.persist(groupOwner);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> findGroupsOwnedBy(EntityManager em,
			String compositeCourseID, User user) {
		List<Group> list = em.createNamedQuery("GroupOwner.findAllCourseGroups")
		.setParameter("compositeCourseID", compositeCourseID)
		.setParameter("user", user)
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersInGroupTree(EntityManager em,
			String courseInstanceID, String relativePath) {
		List<User> list = em.createNamedQuery("Group.findGroupTreeUsers")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("eqRelativePath", relativePath)
		.setParameter("likeRelativePath", relativePath+"/%")
		.getResultList();
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersInGroupTree(EntityManager em,
			String courseInstanceID, String relativePath,
			String likeLastName) {
		List<User> list = em.createNamedQuery("Group.findGroupTreeUsers2")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("eqRelativePath", relativePath)
		.setParameter("likeRelativePath", relativePath+"/%")
		.setParameter("lastName", likeLastName+"%")
		.getResultList();
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersInGroupTree(EntityManager em,
			String courseInstanceID, String relativePath,
			String eqLastName, String likeFirstName) {
		List<User> list = em.createNamedQuery("Group.findGroupTreeUsers3")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("eqRelativePath", relativePath)
		.setParameter("likeRelativePath", relativePath+"/%")
		.setParameter("lastName", eqLastName)
		.setParameter("firstName", likeFirstName+"%")
		.getResultList();
		return list;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersInGroupTree(EntityManager em,
			String courseInstanceID, String relativePath,
			String eqLastName, String eqFirstName, String likeJmbag) {
		List<User> list = em.createNamedQuery("Group.findGroupTreeUsers4")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("eqRelativePath", relativePath)
		.setParameter("likeRelativePath", relativePath+"/%")
		.setParameter("lastName", eqLastName)
		.setParameter("firstName", eqFirstName)
		.setParameter("jmbag", likeJmbag+"%")
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> listUserGroupsInGroupTree(EntityManager em,
			String courseInstanceID, String relativePath) {
		List<UserGroup> list = em.createNamedQuery("Group.findGroupTreeUserGroups")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("eqRelativePath", relativePath)
		.setParameter("likeRelativePath", relativePath+"/%")
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Group> listMarketPlaceRootGroupsForCourse(EntityManager em,
			CourseInstance courseInstance) {
		List<Group> list = em.createNamedQuery("Group.findMarketPlaceRootsOnCourse")
		.setParameter("compositeCourseID", courseInstance.getId())
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> findUserGroupsForUser(EntityManager em,
			String courseInstanceID, String parentRelativePath, User user) {
		List<UserGroup> list = em.createNamedQuery("Group.findUGForUser")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("relativePath", parentRelativePath+"/%")
		.setParameter("user", user)
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getGroupStat(EntityManager em,
			String courseInstanceID, String parentRelativePath) {
		List<Object[]> list = em.createNamedQuery("Group.getGroupStat")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("relativePath", parentRelativePath+"/%")
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCoarseGroupStat(EntityManager em,
			String courseInstanceID, String parentRelativePath) {
		List<Object[]> list = em.createNamedQuery("Group.getCoarseGroupStat")
		.setParameter("compositeCourseID", courseInstanceID)
		.setParameter("relativePath", parentRelativePath+"/%")
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CoarseGroupStat2> getCoarseGroupStat2(EntityManager em,
			String likeCompositeCourseID, String likeRelativePath) {
		List<CoarseGroupStat2> list = em.createNamedQuery("Group.getCoarseGroupStat2")
		.setParameter("compositeCourseID", likeCompositeCourseID)
		.setParameter("relativePath", likeRelativePath)
		.getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> findUserGroup(EntityManager em,
			String likeCourseInstanceID, String likeRelativePath) {
		List<UserGroup> list = em.createNamedQuery("UserGroup.find")
		.setParameter("compositeCourseID", likeCourseInstanceID)
		.setParameter("likeRelativePath", likeRelativePath)
		.getResultList();
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GroupOwner getGroupOwner(EntityManager em, Group group, User user) {
		List<GroupOwner> list = em.createNamedQuery("GroupOwner.getGroupOwner")
			.setParameter("group", group)
			.setParameter("user", user)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listCoursesForUser(EntityManager em,
			String relativePath, User user) {
			List<String> list = em.createNamedQuery("Group.listCoursesForUser")
			.setParameter("relativePath", relativePath)
			.setParameter("user", user)
			.getResultList();
		if(list==null || list.isEmpty()) return null;
		return list;
	}
}
