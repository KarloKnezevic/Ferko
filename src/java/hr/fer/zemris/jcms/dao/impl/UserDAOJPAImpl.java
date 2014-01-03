package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.UserDAO;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

public class UserDAOJPAImpl implements UserDAO {

	@Override
	public User getUserById(EntityManager em, Long id) {
		return em.find(User.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getUserByJMBAG(EntityManager em, String jmbag) {
		List<User> list = em.createNamedQuery("User.findByJMBAG").setParameter("jmbag", jmbag).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getUserByUsername(EntityManager em, String username) {
		List<User> list = em.createNamedQuery("User.findByUsername").setParameter("username", username).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getFullUserByUsername(EntityManager em, String username) {
		List<User> list = em.createNamedQuery("User.findByUsernameFull").setParameter("username", username).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@Override
	public void remove(EntityManager em, User user) {
		em.remove(user);
	}
	
	@Override
	public void save(EntityManager em, User user) {
		em.persist(user);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listUsersOnCourseInstance(EntityManager em,
			String compositeCourseID) {
		List<User> list = em.createNamedQuery("Group.findCourseUsers").setParameter("compositeCourseID", compositeCourseID).getResultList();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getForJmbagSublist(EntityManager em, List<String> jmbags,
			int startIndex, int endIndex) {
		if(endIndex<startIndex) return new ArrayList<User>();
		StringBuilder sb = new StringBuilder(1000);
		sb.append("SELECT u FROM User as u WHERE u.jmbag IN (");
		sb.append("'").append(jmbags.get(startIndex)).append("'");
		startIndex++;
		for(int i = startIndex; i <= endIndex; i++) {
			sb.append(",'").append(jmbags.get(i)).append("'");
		}
		sb.append(")");
		return (List<User>)em.createQuery(sb.toString()).getResultList();
	}
	
	@Override
	public List<User> getForJmbagSublistBatching(EntityManager em,
			List<String> jmbags) {
		List<User> result = new ArrayList<User>(jmbags.size());
		int stepSize = 50;
		int start = 0;
		while(start < jmbags.size()) {
			int end = start + stepSize;
			if(end >= jmbags.size()) {
				end = jmbags.size() -1;
			}
			List<User> sublist = getForJmbagSublist(em, jmbags, start, end);
			result.addAll(sublist);
			start += stepSize;
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> findForGroupAndSubGroups(EntityManager em,
			String compositeCourseID, String likeRelativePath,
			String eqRelativePath) {
		return (List<UserGroup>)em.createNamedQuery("UserGroup.findForGroupAndSubGroups")
			.setParameter("compositeCourseID", compositeCourseID)
			.setParameter("likeRelativePath", likeRelativePath)
			.setParameter("eqRelativePath", eqRelativePath)
			.getResultList();
	}
	@Override
	public Number getUserNumber(EntityManager em, String compositeCourseID,
			String likeRelativePath, String eqRelativePath) {
		return (Number)em.createNamedQuery("UserGroup.getUserNumber")
		.setParameter("compositeCourseID", compositeCourseID)
		.setParameter("likeRelativePath", likeRelativePath)
		.setParameter("eqRelativePath", eqRelativePath)
		.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getFullForJmbagSublist(EntityManager em, List<String> jmbags,
			int startIndex, int endIndex) {
		if(endIndex<startIndex) return new ArrayList<User>();
		StringBuilder sb = new StringBuilder(1000);
		sb.append("SELECT u FROM User as u JOIN FETCH u.userDescriptor WHERE u.jmbag IN (");
		sb.append("'").append(jmbags.get(startIndex)).append("'");
		startIndex++;
		for(int i = startIndex; i <= endIndex; i++) {
			sb.append(",'").append(jmbags.get(i)).append("'");
		}
		sb.append(")");
		return (List<User>)em.createQuery(sb.toString()).getResultList();
	}
	
	@Override
	public List<User> getFullForJmbagSublistBatching(EntityManager em,
			List<String> jmbags) {
		List<User> result = new ArrayList<User>(jmbags.size());
		int stepSize = 50;
		int start = 0;
		while(start < jmbags.size()) {
			int end = start + stepSize;
			if(end >= jmbags.size()) {
				end = jmbags.size() -1;
			}
			List<User> sublist = getFullForJmbagSublist(em, jmbags, start, end);
			result.addAll(sublist);
			start += stepSize;
		}
		return result;
	}
	
}
