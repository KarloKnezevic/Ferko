package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.UserGroupDAO;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.List;

import javax.persistence.EntityManager;

public class UserGroupDAOJPAImpl implements UserGroupDAO {

	@Override
	public UserGroup get(EntityManager em, Long id) {
		return em.find(UserGroup.class, id);
	}

	@Override
	public void remove(EntityManager em, UserGroup userGroup) {
		em.remove(userGroup);
	}

	@Override
	public void save(EntityManager em, UserGroup userGroup) {
		em.persist(userGroup);
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserGroup find(EntityManager em, User user, Group group) {
		List<UserGroup> uglist = (List<UserGroup>)em.createNamedQuery("UserGroup.findForUserGroup")
			.setParameter("user", user)
			.setParameter("group", group)
			.getResultList();
		if(uglist==null || uglist.isEmpty()) return null;
		return uglist.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserGroup> search(EntityManager em, String term) {
		term = "%" + term + "%";
		List<UserGroup> list = (List<UserGroup>)em.createNamedQuery("UserGroup.search")
		.setParameter("term", term).setMaxResults(30).getResultList();
		return list;
	}
}
