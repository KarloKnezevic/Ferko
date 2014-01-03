package hr.fer.zemris.jcms.dao.impl;

import hr.fer.zemris.jcms.dao.AuthTypeDAO;
import hr.fer.zemris.jcms.model.AuthType;

import java.util.List;

import javax.persistence.EntityManager;

public class AuthTypeDAOJPAImpl implements AuthTypeDAO {

	@SuppressWarnings("unchecked")
	@Override
	public AuthType getByName(EntityManager em, String name) {
		List<AuthType> list = em.createNamedQuery("AuthType.findByName").setParameter("name", name).getResultList();
		if(list==null || list.isEmpty()) return null;
		return list.get(0);
	}

	@Override
	public AuthType get(EntityManager em, Long id) {
		return em.find(AuthType.class, id);
	}
	
	@Override
	public void remove(EntityManager em, AuthType authType) {
		em.remove(authType);
	}
	
	@Override
	public void save(EntityManager em, AuthType authType) {
		em.persist(authType);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AuthType> list(EntityManager em) {
		return (List<AuthType>)em.createNamedQuery("AuthType.list").getResultList();
	}
}
