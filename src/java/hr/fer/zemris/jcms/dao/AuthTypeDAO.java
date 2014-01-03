package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.AuthType;

import java.util.List;

import javax.persistence.EntityManager;

public interface AuthTypeDAO {

	public AuthType getByName(EntityManager em, String name);
	public AuthType get(EntityManager em, Long id);
	public void save(EntityManager em, AuthType authType);
	public void remove(EntityManager em, AuthType authType);
	public List<AuthType> list(EntityManager em);
}
