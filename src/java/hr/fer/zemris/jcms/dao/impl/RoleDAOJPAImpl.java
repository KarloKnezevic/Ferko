package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.RoleDAO;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.User;

import java.util.List;

import javax.persistence.EntityManager;

public class RoleDAOJPAImpl implements RoleDAO {

	@Override
	public Role get(EntityManager em, String name) {
		return em.find(Role.class, name);
	}

	@Override
	public void remove(EntityManager em, Role role) {
		em.remove(role);
	}
	
	@Override
	public void save(EntityManager em, Role role) {
		em.persist(role);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Role> list(EntityManager em) {
		return (List<Role>)em.createNamedQuery("Role.list")
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listWithRole(EntityManager em, String roleName) {
		return (List<User>)em.createNamedQuery("role.listWithRole")
			.setParameter("roleName", roleName)
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listWithRole(EntityManager em, String roleName,
			String eqLastName, String eqFirstName, String likeJmbag) {
		return (List<User>)em.createNamedQuery("role.listWithRole4")
		.setParameter("roleName", roleName)
		.setParameter("lastName", eqLastName)
		.setParameter("firstName", eqFirstName)
		.setParameter("jmbag", likeJmbag+"%")
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listWithRole(EntityManager em, String roleName,
			String eqLastName, String likeFirstName) {
		return (List<User>)em.createNamedQuery("role.listWithRole3")
		.setParameter("roleName", roleName)
		.setParameter("lastName", eqLastName)
		.setParameter("firstName", likeFirstName+"%")
		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listWithRole(EntityManager em, String roleName,
			String likeLastName) {
		return (List<User>)em.createNamedQuery("role.listWithRole2")
		.setParameter("roleName", roleName)
		.setParameter("lastName", likeLastName+"%")
		.getResultList();
	}
}
